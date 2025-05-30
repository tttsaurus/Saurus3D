package com.tttsaurus.saurus3d.common.core.mesh.attribute;

import com.tttsaurus.saurus3d.common.core.gl.exception.GLIllegalStateException;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

public class Stride
{
    protected final Deque<Slot> slotStack = new ArrayDeque<>();
    private final int size;
    private int cumulativeSize = 0;

    public int getSize() { return size; }
    public int getCumulativeSize() { return cumulativeSize; }

    public Stride(int size)
    {
        if (size < 0)
            throw new GLIllegalStateException("Stride size cannot be less than 0.");

        this.size = size;
    }

    public Stride push(Slot slot)
    {
        if (cumulativeSize + slot.getSize() > size)
            throw new GLIllegalStateException(String.format("The maximum stride size is %d and you (%d) are exceeding it.", size, cumulativeSize + slot.getSize()));

        cumulativeSize += slot.getSize();
        slotStack.push(slot);

        return this;
    }
    @Nullable
    public Slot pop()
    {
        if (slotStack.peek() == null) return null;

        Slot slot = slotStack.pop();
        cumulativeSize -= slot.getSize();
        return slot;
    }
}
