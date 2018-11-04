package com.example.android.virtualgraffiti.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import com.viro.core.ARAnchor;
import com.viro.core.ARImageTarget;
import com.viro.core.ARNode;
import com.viro.core.ARScene;
import com.viro.core.Material;
import com.viro.core.Node;
import com.viro.core.Quad;
import com.viro.core.Text;
import com.viro.core.Texture;
import com.viro.core.Vector;
import com.viro.core.ViroContext;
import com.viro.core.ViroView;
import com.viro.core.ViroViewARCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ScannerActivity extends Activity implements ARScene.Listener {
    private static final String TAG = ScannerActivity.class.getSimpleName();
    private ViroView mViroView;
    private ViroContext viroContext;
    private ARScene mScene;
    private Bitmap testBitMap;
    private String usrCaption;
    private boolean isPhoto;
    private File imageFile;
    private Bitmap imageTarget;
    private Text usrText;
    private Bitmap imageNode;
    Texture testTexture;
    Material testMaterial;

    Quad testSurface;
    private Map<String, Pair<ARImageTarget, Node>> mTargetedNodesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent.hasExtra("keyName")) {
            usrCaption = intent.getExtras().getString("keyName");
            imageFile = Environment.getExternalStorageDirectory();
            imageTarget = BitmapFactory.decodeFile(imageFile + "/pic.jpg");
            Text.TextBuilder textBuilder = new Text.TextBuilder();
            textBuilder.viroContext(viroContext);
            textBuilder.textString(usrCaption);
            textBuilder.height(0.2f);
            textBuilder.width((0.2f));
            usrText = textBuilder.build();
            isPhoto = false;
        }
        else {
            isPhoto = true;
            imageFile = Environment.getExternalStorageDirectory();
            imageNode = BitmapFactory.decodeFile(imageFile + "/photo_node.jpg");
            imageTarget = BitmapFactory.decodeFile(imageFile + "/pic.jpg");
        }
        mTargetedNodesMap = new HashMap<String, Pair<ARImageTarget, Node>>();
        mViroView = new ViroViewARCore(this, new ViroViewARCore.StartupListener() {
            @Override
            public void onSuccess() {
                if (isPhoto) {
                    onRenderCreate(imageTarget, imageNode);
                } else {
                    onRenderCreate(imageTarget, usrText);
                }
            }

            @Override
            public void onFailure(ViroViewARCore.StartupError error, String errorMessage) {
                Log.e(TAG, "Error initializing AR [" + errorMessage + "]");
            }
        });
        setContentView(mViroView);
    }

    private void onRenderCreate(Bitmap targetImage, Bitmap nodeImage) {
        // Create the base ARScene
        mScene = new ARScene();
        mScene.setListener(this);
        mViroView.setScene(mScene);

        
        Bitmap usrTargetImage = targetImage;
        ARImageTarget userImageTarget = new ARImageTarget(usrTargetImage, ARImageTarget.Orientation.Up, 0.188f);
        mScene.addARImageTarget(userImageTarget);


        Node userNode = new Node();
        testBitMap = nodeImage;
        testTexture = new Texture(testBitMap, Texture.Format.RGBA8, true, true);
        testMaterial = new Material();
        testMaterial.setDiffuseTexture(testTexture);
        testSurface = new Quad(0.2f, 0.2f);
        testSurface.setMaterials(Arrays.asList(testMaterial));
        userNode.setGeometry(testSurface);
        //initCarModel(userNode);
        //initColorPickerModels(userNode);
        //initSceneLights(userNode);
        userNode.setVisible(false);
        mScene.getRootNode().addChildNode(userNode);

        // Link the Node with the ARImageTarget, such that when the image target is
        // found, we'll render the Node.
        linkTargetWithNode(userImageTarget, userNode);
    }


    private void onRenderCreate(Bitmap targetImage, Text text) {
        // Create the base ARScene
        mScene = new ARScene();
        mScene.setListener(this);
        mViroView.setScene(mScene);

        Bitmap usrTargetImage = targetImage;
        ARImageTarget userImageTarget = new ARImageTarget(usrTargetImage, ARImageTarget.Orientation.Up, 0.188f);
        mScene.addARImageTarget(userImageTarget);

        Node userNode = new Node();
        userNode.setGeometry(text);
        userNode.setVisible(false);
        mScene.getRootNode().addChildNode(userNode);
        linkTargetWithNode(userImageTarget, userNode);
    }


    private void linkTargetWithNode(ARImageTarget imageToDetect, Node nodeToRender){
        String key = imageToDetect.getId();
        mTargetedNodesMap.put(key, new Pair(imageToDetect, nodeToRender));
    }


    @Override
    public void onAnchorFound(ARAnchor anchor, ARNode arNode) {
        String anchorId = anchor.getAnchorId();
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            return;
        }

        Node imageTargetNode = mTargetedNodesMap.get(anchorId).second;
        Vector rot = new Vector(0,anchor.getRotation().y, 0);
        imageTargetNode.setPosition(anchor.getPosition());
        imageTargetNode.setRotation(rot);
        imageTargetNode.setVisible(true);
        //animateCarVisible(mCarModelNode);

        // Stop the node from moving in place once found
        ARImageTarget imgTarget = mTargetedNodesMap.get(anchorId).first;
        mScene.removeARImageTarget(imgTarget);
        mTargetedNodesMap.remove(anchorId);
    }

    @Override
    public void onAnchorRemoved(ARAnchor anchor, ARNode arNode) {
        String anchorId = anchor.getAnchorId();
        if (!mTargetedNodesMap.containsKey(anchorId)) {
            return;
        }

        Node imageTargetNode = mTargetedNodesMap.get(anchorId).second;
        imageTargetNode.setVisible(false);
    }

    @Override
    public void onAnchorUpdated(ARAnchor anchor, ARNode arNode) {
        // No-op
    }

    private Bitmap getBitmapFromAssets(final String assetName) {
        final InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = getAssets().open(assetName);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Loading bitmap failed!", e);
        }
        return bitmap;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViroView.onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViroView.onActivityResumed(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mViroView.onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViroView.onActivityStopped(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mViroView.onActivityDestroyed(this);
    }

    @Override
    public void onTrackingInitialized() {
        // No-op
    }

    @Override
    public void onTrackingUpdated(ARScene.TrackingState state, ARScene.TrackingStateReason reason) {
        // No-op
    }

    @Override
    public void onAmbientLightUpdate(float value, Vector v) {
        // No-op
    }
}
