function initializeCoreMod() {
// Unused, remove
    return {
        'KotlinPatcher': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.fml.javafmlmod.FMLModContainer',
                'methodName': 'constructMod',
                'methodDesc': '(Lnet/minecraftforge/fml/LifecycleEventProvider$LifecycleEvent;)V'
            },
            'transformer': function (methodNode) {
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var list = new InsnList();
                list.add(new FieldInsnNode(Opcodes.GETSTATIC, "thedarkcolour/kotlinforforge/AutoKotlinEventBusSubscriber", "INSTANCE", "Lthedarkcolour/kotlinforforge/AutoKotlinEventBusSubscriber;"));
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new FieldInsnNode(Opcodes.GETFIELD, 'net/minecraftforge/fml/javafmlmod/FMLModContainer', 'scanResults', 'Lnet/minecraftforge/forgespi/language/ModFileScanData;'));
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new FieldInsnNode(Opcodes.GETFIELD, 'net/minecraftforge/fml/javafmlmod/FMLModContainer', 'modClass', 'Ljava/lang/Class;'));
                list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'java/lang/Class', 'getClassLoader', '()Ljava/lang/ClassLoader;', false));
                list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "thedarkcolour/kotlinforforge/AutoKotlinEventBusSubscriber", "inject", "(Lnet/minecraftforge/fml/ModContainer;Lnet/minecraftforge/forgespi/language/ModFileScanData;Ljava/lang/ClassLoader;)V", false));

                for (var i = 0; i < 1000; ++i) {
                    var insn = methodNode.instructions.get(i);
                    //print('bruh moment');
                    if (insn instanceof MethodInsnNode) {
                        //print('FOUND A METHODINSNNODE');
                        if (insn.desc === '(Lnet/minecraftforge/fml/ModContainer;Lnet/minecraftforge/forgespi/language/ModFileScanData;Ljava/lang/ClassLoader;)V') {
                            methodNode.instructions.insertBefore(insn.getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious(), list);
                            //print('PATCHED FMLMODCONTAINER');
                            break;
                        }
                    }
                }

                //var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                //methodNode.accept(writer);
                //var reader = new ClassReader(writer.toByteArray());
                //var cn = new ClassNode();
                //reader.accept(cn, 0);

                return methodNode;
            }
        }
    }
}