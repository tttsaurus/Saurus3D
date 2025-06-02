package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import com.tttsaurus.saurus3d.mcpatches.api.texturemap.TexRect;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class RectMergeAlgorithm
{
    public static List<TexRect> mergeRects(List<TexRect> rects)
    {
        List<TexRect> input = new ArrayList<>(rects);
        boolean changed = true;

        while (changed)
        {
            changed = false;
            List<TexRect> mergedList = new ArrayList<>();
            boolean[] used = new boolean[input.size()];

            for (int i = 0; i < input.size(); i++)
            {
                if (used[i]) continue;
                TexRect a = input.get(i);
                boolean mergedThisRound = false;

                for (int j = i + 1; j < input.size(); j++)
                {
                    if (used[j]) continue;
                    TexRect b = input.get(j);
                    TexRect merged = tryMerge(a, b);

                    if (merged != null)
                    {
                        mergedList.add(merged);
                        used[i] = true;
                        used[j] = true;
                        mergedThisRound = true;
                        changed = true;
                        break;
                    }
                }

                if (!mergedThisRound)
                {
                    mergedList.add(a);
                    used[i] = true;
                }
            }

            for (int i = 0; i < input.size(); i++)
                if (!used[i])
                    mergedList.add(input.get(i));

            input = mergedList;
        }

        return input;
    }

    private static TexRect tryMerge(TexRect a, TexRect b)
    {
        if (a.y == b.y && a.height == b.height)
        {
            if (a.x + a.width == b.x || b.x + b.width == a.x)
            {
                int left = Math.min(a.x, b.x);
                int width = a.width + b.width;
                return new TexRect(left, a.y, width, a.height);
            }
        }
        if (a.x == b.x && a.width == b.width)
        {
            if (a.y + a.height == b.y || b.y + b.height == a.y)
            {
                int top = Math.min(a.y, b.y);
                int height = a.height + b.height;
                return new TexRect(a.x, top, a.width, height);
            }
        }
        return null;
    }

    public static boolean isPerfectRect(List<TexRect> rects)
    {
        if (rects.isEmpty()) return false;

        int x1 = Integer.MAX_VALUE;
        int x2 = Integer.MIN_VALUE;
        int y1 = Integer.MAX_VALUE;
        int y2 = Integer.MIN_VALUE;

        HashSet<String> set = new HashSet<>();
        int area = 0;

        for (TexRect rect : rects)
        {
            x1 = Math.min(rect.x, x1);
            y1 = Math.min(rect.y, y1);
            x2 = Math.max(rect.x + rect.width, x2);
            y2 = Math.max(rect.y + rect.height, y2);

            area += rect.width * rect.height;

            String s1 = rect.x + " " + rect.y;
            String s2 = rect.x + " " + (rect.y + rect.height);
            String s3 = (rect.x + rect.width) + " " + (rect.y + rect.height);
            String s4 = (rect.x + rect.width) + " " + rect.y;

            if (!set.add(s1)) set.remove(s1);
            if (!set.add(s2)) set.remove(s2);
            if (!set.add(s3)) set.remove(s3);
            if (!set.add(s4)) set.remove(s4);
        }

        if (!set.contains(x1 + " " + y1) || !set.contains(x1 + " " + y2) || !set.contains(x2 + " " + y1) || !set.contains(x2 + " " + y2) || set.size() != 4) return false;

        return area == (x2 - x1) * (y2 - y1);
    }
}
