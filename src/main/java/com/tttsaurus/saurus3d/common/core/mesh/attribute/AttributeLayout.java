package com.tttsaurus.saurus3d.common.core.mesh.attribute;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class AttributeLayout
{
    private final Deque<Stride> strideStack = new ArrayDeque<>();

    public AttributeLayout push(Stride stride)
    {
        strideStack.push(stride);
        return this;
    }
    @Nullable
    public Stride pop()
    {
        if (strideStack.peek() == null) return null;
        return strideStack.pop();
    }

    public String getDebugReport()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("\n===Attribute Layout Debug Report===\n");

        int attributeIndex = 0;

        int strideIndex = 0;
        Iterator<Stride> strideIter = strideStack.descendingIterator();
        while (strideIter.hasNext())
        {
            Stride stride = strideIter.next();
            builder.append("Stride ").append(strideIndex).append(" (").append(stride.getSize()).append(" bytes):");

            int usedSlotSize = 0;
            Iterator<Slot> slotIter = stride.slotStack.descendingIterator();
            while (slotIter.hasNext())
            {
                Slot slot = slotIter.next();

                builder.append("\n");
                builder.append("- Attribute ").append(attributeIndex).append(" ");
                if (slot.getInterpretationType() == InterpretationType.TO_FLOAT_KIND)
                    builder.append("[FLOAT_KIND]");
                else if (slot.getInterpretationType() == InterpretationType.TO_INT_KIND)
                    builder.append("[INT_KIND]");
                builder.append(": ");
                builder.append(slot.getCount()).append(" * ").append(slot.getType().toString());
                builder.append("; ").append(slot.getSize()).append(" bytes");
                builder.append("; offset ").append(usedSlotSize).append(" bytes");
                if (slot.getDivisor() != 0)
                    builder.append("; divisor = ").append(slot.getDivisor());
                if (slot.isNormalize() && slot.getInterpretationType() == InterpretationType.TO_FLOAT_KIND)
                    builder.append("; normalize");

                attributeIndex++;
                usedSlotSize += slot.getSize();
            }
            strideIndex++;
            builder.append("\n");
        }

        builder.append("\n===End of the Debug Report===");

        return builder.toString();
    }

    public void uploadToGL()
    {
        int attributeIndex = 0;

        Iterator<Stride> strideIter = strideStack.descendingIterator();
        while (strideIter.hasNext())
        {
            Stride stride = strideIter.next();

            int usedSlotSize = 0;
            Iterator<Slot> slotIter = stride.slotStack.descendingIterator();
            while (slotIter.hasNext())
            {
                Slot slot = slotIter.next();

                if (slot.getInterpretationType() == InterpretationType.TO_FLOAT_KIND)
                {
                    GL20.glVertexAttribPointer(
                            attributeIndex,
                            slot.getCount(),
                            slot.getType().glValue,
                            slot.isNormalize(),
                            stride.getSize(),
                            usedSlotSize);
                }
                else if (slot.getInterpretationType() == InterpretationType.TO_INT_KIND)
                {
                    GL30.glVertexAttribIPointer(
                            attributeIndex,
                            slot.getCount(),
                            slot.getType().glValue,
                            stride.getSize(),
                            usedSlotSize);
                }
                GL20.glEnableVertexAttribArray(attributeIndex);
                if (slot.getDivisor() != 0)
                    GL33.glVertexAttribDivisor(attributeIndex, slot.getDivisor());

                attributeIndex++;
                usedSlotSize += slot.getSize();
            }
        }
    }
}
