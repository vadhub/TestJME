package com.abg.testjem;

import com.jme3.anim.AnimComposer;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
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

public class MainGame extends SimpleApplication {

    private AnimControl animControl;
    private AnimChannel animChannel;

    private Node ualModel;                         // модель персонажа
    private float cameraDistance = 5f;             // расстояние от персонажа
    private float cameraYaw = 0f;                  // угол поворота (0 – спереди)
    private float cameraPitch = 0f;                // наклон
    private float lookAtOffsetY = 2f;              // смещение точки взгляда вверх

    // Переменные для плавного перемещения
    private Vector3f targetPosition;               // целевая позиция персонажа
    private float moveSpeed = 6f;                  // скорость перемещения (единиц в секунду)

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.8f, 1f, 1f));

        setupLightAndShadows();
        createRoad();

        ualModel = (Node) assetManager.loadModel("Models/UAL/UAL1_Standard.glb");

        // Настройка материала и поворота
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

        // Инициализация целевой позиции
        targetPosition = ualModel.getLocalTranslation().clone();

        flyCam.setEnabled(false);
        updateCameraPosition();

        // Слушатель свайпов
        inputManager.addRawInputListener(new com.jme3.input.RawInputListener() {
            private float startX = 0;
            private boolean touching = false;
            private final float SWIPE_THRESHOLD = 100f;

            @Override
            public void onTouchEvent(TouchEvent event) {
                if (event.getType() == TouchEvent.Type.DOWN) {
                    startX = event.getX();
                    touching = true;
                } else if (event.getType() == TouchEvent.Type.UP && touching) {
                    float deltaX = event.getX() - startX;
                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        float moveDistance = 2f;   // шаг вправо/влево
                        float currentX = ualModel.getLocalTranslation().x;
                        float newX = currentX + (deltaX > 0 ? moveDistance : -moveDistance);
                        newX = FastMath.clamp(newX, -2f, 2f);
                        targetPosition.setX(newX);
                    }
                    touching = false;
                }
            }
            @Override public void beginInput() {}
            @Override public void endInput() {}
            @Override public void onMouseMotionEvent(MouseMotionEvent evt) {}
            @Override public void onMouseButtonEvent(MouseButtonEvent evt) {}
            @Override public void onKeyEvent(KeyInputEvent evt) {}
            @Override public void onJoyAxisEvent(JoyAxisEvent evt) {}
            @Override public void onJoyButtonEvent(JoyButtonEvent evt) {}
        });

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        ualModel.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Анимация через AnimComposer
        AnimComposer animComposer = findAnimComposer(ualModel);
        if (animComposer != null) {
            animComposer.setCurrentAction("Sprint_Loop");
        } else {
            System.err.println("AnimComposer не найден!");
        }

        // Устаревший AnimControl (для информации)
        animControl = ualModel.getControl(AnimControl.class);
        if (animControl != null) {
            animChannel = animControl.createChannel();
            System.out.println("--- Доступные анимации ---");
            for (String animName : animControl.getAnimationNames()) {
                System.out.println(animName);
            }
            if (!animControl.getAnimationNames().isEmpty()) {
                String firstAnimName = animControl.getAnimationNames().iterator().next();
                animChannel.setAnim(firstAnimName, 1.0f);
                animChannel.setLoopMode(LoopMode.Loop);
            }
        } else {
            System.err.println("AnimControl не найден!");
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f currentPos = ualModel.getLocalTranslation();
        if (!currentPos.equals(targetPosition)) {
            // Вычисляем направление к цели
            Vector3f direction = targetPosition.subtract(currentPos).normalizeLocal();
            // Смещение за кадр
            float step = moveSpeed * tpf;
            Vector3f newPos = currentPos.add(direction.mult(step));

            // Если перескочили цель — ставим точно в цель
            if (newPos.x * direction.x > targetPosition.x * direction.x && direction.x != 0) {
                ualModel.setLocalTranslation(targetPosition);
            } else {
                ualModel.setLocalTranslation(newPos);
            }
            updateCameraPosition();
        }
    }

    private void setupLightAndShadows() {
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.5f).normalizeLocal());
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.Gray);
        rootNode.addLight(ambient);

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 1024, 3);
        dlsr.setLight(sun);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCF4);
        viewPort.addProcessor(dlsr);
    }

    public AnimComposer findAnimComposer(Spatial spatial) {
        AnimComposer composer = spatial.getControl(AnimComposer.class);
        if (composer != null) return composer;
        if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                composer = findAnimComposer(child);
                if (composer != null) return composer;
            }
        }
        return null;
    }

    private void createRoad() {
        float roadWidth = 4f;
        float roadLength = 30f;
        float roadHeight = 0.1f;

        Geometry road = new Geometry("Road", new com.jme3.scene.shape.Box(roadWidth, roadHeight, roadLength));
        Material roadMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        roadMat.setBoolean("UseMaterialColors", true);
        roadMat.setColor("Diffuse", new ColorRGBA(0.0f, 0.4f, 0.0f, 1.0f));
        roadMat.setColor("Ambient", ColorRGBA.Gray);
        roadMat.setColor("Specular", ColorRGBA.Black);
        road.setMaterial(roadMat);
        road.setLocalTranslation(0, -roadHeight - 0.05f, 0);
        road.setShadowMode(RenderQueue.ShadowMode.Receive);
        rootNode.attachChild(road);
    }

    private void updateCameraPosition() {
        Vector3f modelPos = ualModel.getLocalTranslation();
        float x = modelPos.x + cameraDistance * (float)(Math.sin(cameraYaw) * Math.cos(cameraPitch));
        float y = modelPos.y + cameraDistance + 5 * (float)(Math.sin(cameraPitch));
        float z = modelPos.z + cameraDistance * (float)(Math.cos(cameraYaw) * Math.cos(cameraPitch));
        cam.setLocation(new Vector3f(x, y, z));
        Vector3f lookAtPoint = modelPos.add(0, lookAtOffsetY, 0);
        cam.lookAt(lookAtPoint, Vector3f.UNIT_Y);
    }
}