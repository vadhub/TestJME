package com.abg.testjem;

import com.jme3.anim.AnimComposer;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;

import jme3tools.optimize.GeometryBatchFactory;

public class MainGame extends SimpleApplication {

    private AnimControl animControl;
    private AnimChannel animChannel;

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.8f, 1f, 1f));

        // 1. Настраиваем освещение и тени
        setupLightAndShadows();

        // 2. Загружаем модель
        Node ualModel = (Node) assetManager.loadModel("Models/UAL/UAL1_Standard.glb");

        // 3. Заменяем материал на Lighting (реагирует на свет)
        ualModel.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geometry) {
                Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                mat.setBoolean("UseMaterialColors", true);
                mat.setColor("Diffuse", ColorRGBA.Yellow);
                mat.setColor("Ambient", ColorRGBA.Red);
                mat.setColor("Specular", ColorRGBA.Red);

                geometry.setMaterial(mat);
            }
        });


        ualModel.rotate(0, FastMath.PI, 0);

        rootNode.attachChild(ualModel);

        flyCam.setEnabled(false);

        Vector3f modelPos = ualModel.getLocalTranslation();
        // Задаём углы камеры в сферических координатах
        float distance = 5f;      // расстояние от модели
        float yaw = 0;  // угол поворота вокруг Y (PI = 180° – сзади, 0 – спереди)
        float pitch = 0.f;       // угол наклона вверх-вниз (радианы: 0 – горизонтально, положительные значения – сверху)

        // Вычисляем позицию камеры
        float x = modelPos.x + distance * (float)(Math.sin(yaw) * Math.cos(pitch));
        float y = modelPos.y + distance + 5 * (float)(Math.sin(pitch));
        float z = modelPos.z + distance * (float)(Math.cos(yaw) * Math.cos(pitch));

        cam.setLocation(new Vector3f(x, y, z));

        cam.lookAt(modelPos, Vector3f.UNIT_Y);

        // 4. Разрешаем объектам отбрасывать и принимать тени
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        ualModel.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // 5. Анимация через AnimComposer (современный API)
        AnimComposer animComposer = findAnimComposer(ualModel);
        if (animComposer != null) {
            animComposer.setCurrentAction("Sprint_Loop");
        } else {
            System.err.println("AnimComposer не найден! Проверьте модель.");
        }

        // 6. Устаревший AnimControl (для информации)
        animControl = ualModel.getControl(AnimControl.class);
        if (animControl != null) {
            animChannel = animControl.createChannel();
            System.out.println("--- Доступные анимации (устаревший API) ---");
            for (String animName : animControl.getAnimationNames()) {
                System.out.println(animName);
            }
            if (!animControl.getAnimationNames().isEmpty()) {
                String firstAnimName = animControl.getAnimationNames().iterator().next();
                animChannel.setAnim(firstAnimName, 1.0f);
                animChannel.setLoopMode(LoopMode.Loop);
            }
        } else {
            System.err.println("AnimControl не найден! Проверьте модель.");
        }
    }

    private void setupLightAndShadows() {
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sun);

        // Фоновый свет (чтобы тени не были слишком чёрными)
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.Gray);
        rootNode.addLight(ambient);

        // Тени
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 1024, 3);
        dlsr.setLight(sun);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCF4);
        viewPort.addProcessor(dlsr);
    }

    public AnimComposer findAnimComposer(Spatial spatial) {
        AnimComposer composer = spatial.getControl(AnimComposer.class);
        if (composer != null) {
            return composer;
        }
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