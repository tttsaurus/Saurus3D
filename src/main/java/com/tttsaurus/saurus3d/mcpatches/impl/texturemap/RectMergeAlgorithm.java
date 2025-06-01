package com.tttsaurus.saurus3d.mcpatches.impl.texturemap;

import net.minecraft.util.Tuple;
import java.util.ArrayList;
import java.util.List;

public final class RectMergeAlgorithm
{
    private static Tuple<Integer, Integer> getMaxXY(List<TexRect> texRects)
    {
        int maxX = 0, maxY = 0;
        for (TexRect r: texRects)
        {
            if (r.x + r.width > maxX) maxX = r.x + r.width;
            if (r.y + r.height > maxY) maxY = r.y + r.height;
        }
        return new Tuple<>(maxX, maxY);
    }

    private static boolean[][] genBooleanGrid(List<TexRect> texRects, int maxX, int maxY)
    {
        boolean[][] grid = new boolean[maxY][maxX];
        for (TexRect r: texRects)
        {
            for (int y = r.y; y < r.y + r.height; y++)
                for (int x = r.x; x < r.x + r.width; x++)
                    grid[y][x] = true;
        }
        return grid;
    }

    public static List<TexRect> mergeRects(List<TexRect> texRects)
    {
        List<TexRect> result = new ArrayList<>();

        Tuple<Integer, Integer> xy = getMaxXY(texRects);
        int maxX = xy.getFirst();
        int maxY = xy.getSecond();

        boolean[][] grid = genBooleanGrid(texRects, maxX, maxY);
        boolean[][] visited = new boolean[maxY][maxX];

        for (int y = 0; y < maxY; y++)
        {
            for (int x = 0; x < maxX; x++)
            {
                if (grid[y][x] && !visited[y][x])
                {
                    int width = 0;
                    while (x + width < maxX && grid[y][x + width] && !visited[y][x + width])
                        width++;
                    int height = 0;
                    boolean valid = true;
                    while (y + height < maxY && valid)
                    {
                        for (int dx = 0; dx < width; dx++)
                        {
                            if (!grid[y + height][x + dx] || visited[y + height][x + dx])
                            {
                                valid = false;
                                break;
                            }
                        }
                        if (valid)
                            height++;
                    }

                    for (int dy = 0; dy < height; dy++)
                        for (int dx = 0; dx < width; dx++)
                            visited[y + dy][x + dx] = true;

                    result.add(new TexRect(x, y, width, height));
                }
            }
        }

        return result;
    }
}
