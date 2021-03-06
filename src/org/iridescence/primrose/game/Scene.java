package org.iridescence.primrose.game;

import java.util.ArrayDeque;
import java.util.Iterator;
import org.iridescence.primrose.graphics.Mesh;
import org.iridescence.primrose.graphics.lights.DirectionalLight;
import org.iridescence.primrose.graphics.lights.Light;
import org.iridescence.primrose.graphics.lights.LightType;
import org.iridescence.primrose.graphics.lights.PointLight;
import org.iridescence.primrose.graphics.lights.SpotLight;
import org.iridescence.primrose.graphics.shaders.IntegratedBlinnPhongShader;
import org.iridescence.primrose.graphics.shaders.IntegratedLambertShader;
import org.iridescence.primrose.graphics.sprite.Sprite;
import org.iridescence.primrose.utils.Logging;
import org.joml.Vector3f;

public class Scene {

  ArrayDeque<GameObject> meshes;
  ArrayDeque<GameObject> sprites;
  final ArrayDeque<GameObject> lights;

  final Vector3f ambientColor;
  final float ambientIntensity;
  int directionalCount, pointCount, spotCount;



  boolean[] materialsUsed;

  public Scene() {
    meshes = new ArrayDeque<>();
    lights = new ArrayDeque<>();
    sprites = new ArrayDeque<>();

    ambientColor = new Vector3f(1.0f);
    ambientIntensity = 0.1f;

    directionalCount = 0;
    pointCount = 0;
    spotCount = 0;

    materialsUsed = new boolean[6];

    for(int i = 0; i < 6; i++){
      materialsUsed[i] = false;
    }
  }

  public void add(GameObject object) {
    switch (object.type) {
      case OBJECT_TYPE_MESH:
        if(object.layer >= 16 || object.layer < 0){
          Logging.logger.warning("Cannot have more than 16 layers! GameObject reassigned to layer 0!");
          object.layer = 0;
        }

        meshes.push(object);
        //Check materials we need to actually update!
        materialsUsed[((Mesh) object).material.type.getValue()] = true;

        break;

      case OBJECT_TYPE_SPRITE:
        if(object.layer >= 16 || object.layer < 0){
          Logging.logger.warning("Cannot have more than 16 layers! GameObject reassigned to layer 0!");
          object.layer = 0;
        }
        sprites.push(object);
        materialsUsed[0] = true;
        break;

      case OBJECT_TYPE_LIGHT:
        Light light = (Light) object;
        if (light.type == LightType.LIGHT_TYPE_DIRECTIONAL) {
          directionalCount++;

          if (directionalCount > 4) {
            Logging.logger.warning("Cannot have more than 4 directional lights!");
            directionalCount = 4;
          } else {
            lights.push(object);
          }
        } else if (light.type == LightType.LIGHT_TYPE_POINTLIGHT) {
          pointCount++;

          if (pointCount > 16) {
            Logging.logger.warning("Cannot have more than 16 point lights!");
            pointCount = 16;
          } else {
            lights.push(object);
          }
        } else if (light.type == LightType.LIGHT_TYPE_SPOTLIGHT) {
          spotCount++;

          if (spotCount > 8) {
            Logging.logger.warning("Cannot have more than 16 point lights!");
            spotCount = 8;
          } else {
            lights.push(object);
          }
        }
        updateLighting();
        break;
    }
  }

  private void updateLighting() {
    // Updates light components for each shader.

    if(materialsUsed[1] || materialsUsed[4]){
      IntegratedLambertShader.shader.bind();
      IntegratedLambertShader.shader.setUniformVec3f("ambient.color", ambientColor);
      IntegratedLambertShader.shader.setUniformFloat("ambient.intensity", ambientIntensity);
    }

    if(materialsUsed[2] || materialsUsed[3]){
      IntegratedBlinnPhongShader.shader.bind();
      IntegratedBlinnPhongShader.shader.setUniformVec3f("ambient.color", ambientColor);
      IntegratedBlinnPhongShader.shader.setUniformFloat("ambient.intensity", ambientIntensity);
    }




    Iterator<GameObject> iter = lights.iterator();
    int dirCount = 0;
    int poiCount = 0;
    int spoCount = 0;
    while (iter.hasNext()) {
      Light temp = (Light) iter.next();
      // Make sure lights are correct.
      temp.Update();

      switch (temp.type) {
        case LIGHT_TYPE_DIRECTIONAL:
          DirectionalLight dir = (DirectionalLight) temp;

          if(materialsUsed[1] || materialsUsed[4]){
            IntegratedLambertShader.shader.bind();

            IntegratedLambertShader.shader.setUniformVec3f("directional[" + dirCount + "].color", dir.color);
            IntegratedLambertShader.shader.setUniformFloat(
                "directional[" + dirCount + "].intensity", dir.intensity);
            IntegratedLambertShader.shader.setUniformVec3f(
                "directional[" + dirCount + "].direction", dir.direction);
          }

          if(materialsUsed[2] || materialsUsed[3]){
            IntegratedBlinnPhongShader.shader.bind();

            IntegratedBlinnPhongShader.shader.setUniformVec3f("directional[" + dirCount + "].color", dir.color);
            IntegratedBlinnPhongShader.shader.setUniformFloat(
                "directional[" + dirCount + "].intensity", dir.intensity);
            IntegratedBlinnPhongShader.shader.setUniformVec3f(
                "directional[" + dirCount + "].direction", dir.direction);
          }

          dirCount++;
          break;

        case LIGHT_TYPE_POINTLIGHT:
          PointLight poi = (PointLight) temp;

          if(materialsUsed[1] || materialsUsed[4]){
            IntegratedLambertShader.shader.bind();

            IntegratedLambertShader.shader.setUniformVec3f(
                "pointLights[" + poiCount + "].position", poi.transform.position);
            IntegratedLambertShader.shader.setUniformVec3f("pointLights[" + poiCount + "].color", poi.color);
            IntegratedLambertShader.shader.setUniformFloat(
                "pointLights[" + poiCount + "].intensity", poi.intensity);
            IntegratedLambertShader.shader.setUniformFloat(
                "pointLights[" + poiCount + "].linear", poi.linearTerm);
            IntegratedLambertShader.shader.setUniformFloat(
                "pointLights[" + poiCount + "].quadratic", poi.quadraticTerm);
          }

          if(materialsUsed[2] || materialsUsed[3]){
            IntegratedBlinnPhongShader.shader.bind();
            IntegratedBlinnPhongShader.shader.setUniformVec3f(
                "pointLights[" + poiCount + "].position", poi.transform.position);
            IntegratedBlinnPhongShader.shader.setUniformVec3f("pointLights[" + poiCount + "].color", poi.color);
            IntegratedBlinnPhongShader.shader.setUniformFloat(
                "pointLights[" + poiCount + "].intensity", poi.intensity);
            IntegratedBlinnPhongShader.shader.setUniformFloat(
                "pointLights[" + poiCount + "].linear", poi.linearTerm);
            IntegratedBlinnPhongShader.shader.setUniformFloat(
                "pointLights[" + poiCount + "].quadratic", poi.quadraticTerm);
          }


          poiCount++;
          break;

        case LIGHT_TYPE_SPOTLIGHT:
          SpotLight spo = (SpotLight) temp;

          if(materialsUsed[1] || materialsUsed[4]){
            IntegratedLambertShader.shader.bind();

            IntegratedLambertShader.shader.setUniformVec3f(
                "spotLights[" + spoCount + "].position", spo.transform.position);
            IntegratedLambertShader.shader.setUniformVec3f(
                "spotLights[" + spoCount + "].direction", spo.direction);
            IntegratedLambertShader.shader.setUniformVec3f("spotLights[" + spoCount + "].color", spo.color);
            IntegratedLambertShader.shader.setUniformFloat(
                "spotLights[" + spoCount + "].intensity", spo.intensity);
            IntegratedLambertShader.shader.setUniformFloat("spotLights[" + spoCount + "].linear", spo.linearTerm);
            IntegratedLambertShader.shader.setUniformFloat(
                "spotLights[" + spoCount + "].quadratic", spo.quadraticTerm);
            IntegratedLambertShader.shader.setUniformFloat("spotLights[" + spoCount + "].cutOff", spo.mCutOff);
          }

          if(materialsUsed[2] || materialsUsed[3]){
            IntegratedBlinnPhongShader.shader.bind();

            IntegratedBlinnPhongShader.shader.setUniformVec3f(
                "spotLights[" + spoCount + "].position", spo.transform.position);
            IntegratedBlinnPhongShader.shader.setUniformVec3f(
                "spotLights[" + spoCount + "].direction", spo.direction);
            IntegratedBlinnPhongShader.shader.setUniformVec3f("spotLights[" + spoCount + "].color", spo.color);
            IntegratedBlinnPhongShader.shader.setUniformFloat(
                "spotLights[" + spoCount + "].intensity", spo.intensity);
            IntegratedBlinnPhongShader.shader.setUniformFloat("spotLights[" + spoCount + "].linear", spo.linearTerm);
            IntegratedBlinnPhongShader.shader.setUniformFloat(
                "spotLights[" + spoCount + "].quadratic", spo.quadraticTerm);
            IntegratedBlinnPhongShader.shader.setUniformFloat("spotLights[" + spoCount + "].cutOff", spo.mCutOff);
          }

          spoCount++;
          break;
      }
    }
  }

  public void render() {
    updateLighting();

    for(int i = 0; i < 16; i++){
      for (GameObject obj : meshes) {
        if (obj.layer == i) {
          ((Mesh) obj).render();
        }
      }
    }

    //Sprites are rendered afterwards

    for(int i = 0; i < 16; i++){
      for (GameObject obj : sprites) {
        if (obj.layer == i) {
          ((Sprite) obj).render();
        }
      }
    }

  }

  public GameObject getGameObjectByName(String name){
    GameObject res;

    for(GameObject obj : meshes){
      if(obj.name.equals(name)){
        return obj;
      }
    }

    return null;
  }


  public GameObject getGameObjectByTag(String tag){
    GameObject res;

    for(GameObject obj : meshes){
      if(obj.tag.equals(tag)){
        return obj;
      }
    }

    return null;
  }

  public ArrayDeque<GameObject> getGameObjectsByTag(String tag){
    ArrayDeque<GameObject> res = new ArrayDeque<>();

    for(GameObject obj : meshes){
      if(obj.tag.equals(tag)){
        res.push(obj);
      }
    }
    return res;
  }

  public ArrayDeque<GameObject> getGameObjectsByLayer(short layer){
    ArrayDeque<GameObject> res = new ArrayDeque<>();

    for(GameObject obj : meshes){
      if(obj.layer == layer){
        res.push(obj);
      }
    }
    return res;
  }

}
