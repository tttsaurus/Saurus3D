package com.tttsaurus.saurus3d.common.core.gl.feature;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

public final class GLFeatureASMHelper
{
    private static int getArgumentSlotCount(String methodDesc)
    {
        Type[] argTypes = Type.getArgumentTypes(methodDesc);
        int slotCount = 0;
        for (Type t: argTypes)
            slotCount += t.getSize();
        return slotCount;
    }

    // prevent this.instance getter
    // prevent this.instance setter
    // prevent most of this.method()
    @SuppressWarnings("all")
    private static boolean staticConvertableRoughCheck(Class<?> clazz, String methodName, String methodDesc)
    {
        String className = clazz.getName();
        ClassLoader classLoader = clazz.getClassLoader();

        try
        {
            ClassReader reader = new ClassReader(classLoader.getResourceAsStream(className.replace('.', '/') + ".class"));
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            for (MethodNode method : classNode.methods)
            {
                if (method.name.equals(methodName) && method.desc.equals(methodDesc) && (method.access & Opcodes.ACC_STATIC) == 0)
                {
                    Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());

                    Frame<SourceValue>[] frames = analyzer.analyze(classNode.name, method);
                    AbstractInsnNode[] instructions = method.instructions.toArray();

                    for (int i = 0; i < instructions.length; i++)
                    {
                        AbstractInsnNode insn = instructions[i];
                        Frame<SourceValue> frame = frames[i];
                        if (frame == null) continue;

                        if (insn.getOpcode() == Opcodes.GETFIELD || insn.getOpcode() == Opcodes.PUTFIELD)
                        {
                            int thisIndex = frame.getStackSize() - (insn.getOpcode() == Opcodes.GETFIELD ? 1 : 2);
                            if (thisIndex < 0) continue;

                            SourceValue sv = frame.getStack(thisIndex);
                            for (AbstractInsnNode sourceInsn: sv.insns)
                            {
                                if (sourceInsn.getOpcode() == Opcodes.ALOAD)
                                {
                                    VarInsnNode varInsn = (VarInsnNode) sourceInsn;
                                    if (varInsn.var == 0)
                                        return false;
                                }
                            }
                        }
                        else if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL || insn.getOpcode() == Opcodes.INVOKESPECIAL)
                        {
                            if (!(insn instanceof MethodInsnNode)) continue;
                            int thisIndex = frame.getStackSize() - 1 - getArgumentSlotCount(((MethodInsnNode)insn).desc);
                            if (thisIndex < 0) continue;

                            SourceValue sv = frame.getStack(thisIndex);
                            for (AbstractInsnNode sourceInsn: sv.insns)
                            {
                                if (sourceInsn.getOpcode() == Opcodes.ALOAD)
                                {
                                    VarInsnNode varInsn = (VarInsnNode) sourceInsn;
                                    if (varInsn.var == 0)
                                        return false;
                                }
                            }
                        }
                    }

                    return true;
                }
            }
        }
        catch (Throwable ignored)
        {
            return false;
        }

        return false;
    }

    // make 'isSupported' method static
    @SuppressWarnings("all")
    @Nullable
    protected static Class<?> generateFeatureHelperClass(Class<? extends IGLFeature> featureClass)
    {
        if (!staticConvertableRoughCheck(featureClass, "isSupported", "()Z")) return null;

        Class<?> featureHelperClass = null;

        String oldClassName = featureClass.getName();
        String newClassName = oldClassName.replace(featureClass.getSimpleName(), featureClass.getSimpleName() + "StaticHelper");

        ClassLoader classLoader = featureClass.getClassLoader();

        try
        {
            ClassReader reader = new ClassReader(classLoader.getResourceAsStream(oldClassName.replace('.', '/') + ".class"));
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, writer)
            {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
                {
                    super.visit(version, access, newClassName.replace('.', '/'), signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
                {
                    if (name.equals("isSupported") && (access & Opcodes.ACC_STATIC) == 0)
                    {
                        access = (access | Opcodes.ACC_STATIC);
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                    return null;
                }
            };

            reader.accept(visitor, 0);

            byte[] bytes = writer.toByteArray();

            Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClassMethod.setAccessible(true);
            featureHelperClass = (Class<?>)defineClassMethod.invoke(classLoader, newClassName, bytes, 0, bytes.length);
        }
        catch (Throwable throwable)
        {
            return null;
        }

        return featureHelperClass;
    }
}
