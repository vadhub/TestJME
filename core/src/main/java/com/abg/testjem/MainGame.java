package com.abg.testjem;

import com.jme3.anim.AnimComposer;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class MainGame extends SimpleApplication {

    // 1. Объявляем переменные для управления анимацией
    private AnimControl animControl;
    private AnimChannel animChannel;

    @Override
    public void simpleInitApp() {
        // Настройка базового освещения
        setupLight();

        // 2. Загрузка GLB-модели с анимациями
        // assetManager загрузит модель и все анимации
        Node ualModel = (Node) assetManager.loadModel("Models/UAL/UAL1_Standard.glb");
        rootNode.attachChild(ualModel);

        AnimComposer animComposer = findAnimComposer(ualModel);

        if (animComposer != null) {
            // Пытаемся запустить анимацию по её имени.
            // Убедитесь, что имя написано точно так же, как в файле.
            animComposer.setCurrentAction("Idle_Loop");
        } else {
            System.err.println("AnimComposer не найден! Проверьте модель.");
        }

        // 3. Пробуем найти AnimControl
        // Он "прикреплен" к модели после загрузки.
        animControl = ualModel.getControl(AnimControl.class);

        if (animControl != null) {
            // 4. Создаем канал для управления анимацией
            animChannel = animControl.createChannel();

            // 5. ВАЖНО: Узнаем, как называются анимации
            // Выводим в консоль имена доступных анимаций
            System.out.println("--- Доступные анимации ---");
            for (String animName : animControl.getAnimationNames()) {
                System.out.println(animName);
            }
            System.out.println("--------------------------");

            // 6. Воспроизводим первую найденную анимацию (или конкретную по имени)
            // Допустим, вы увидели в консоли имя "Idle"
            if (!animControl.getAnimationNames().isEmpty()) {
                String firstAnimName = animControl.getAnimationNames().iterator().next();
                animChannel.setAnim(firstAnimName, 1.0f); // 1.0f - скорость
                animChannel.setLoopMode(LoopMode.Loop); // Зацикливаем анимацию
            } else {
                System.err.println("Анимации не найдены!");
            }
        } else {
            System.err.println("AnimControl не найден! Проверьте модель.");
        }
    }

    private void setupLight() {
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sun);
    }

    public AnimComposer findAnimComposer(Spatial spatial) {
        // Проверяем, есть ли AnimComposer в текущем узле
        AnimComposer composer = spatial.getControl(AnimComposer.class);
        if (composer != null) {
            return composer;
        }
        // Если узел может содержать детей, рекурсивно проверяем их
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                composer = findAnimComposer(child);
                if (composer != null) {
                    return composer;
                }
            }
        }
        return null;
    }
}
