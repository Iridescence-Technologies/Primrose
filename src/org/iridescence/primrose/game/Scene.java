package org.iridescence.primrose.game;

import java.util.ArrayDeque;
import java.util.Iterator;
import org.iridescence.primrose.graphics.Mesh;
import org.iridescence.primrose.graphics.lights.DirectionalLight;
import org.iridescence.primrose.graphics.lights.Light;
import org.iridescence.primrose.graphics.lights.LightType;
import org.iridescence.primrose.graphics.lights.PointLight;
import org.iridescence.primrose.graphics.lights.SpotLight;
import org.iridescence.primrose.graphics.shaders.PhongShader;
import org.iridescence.primrose.utils.Logging;
import org.joml.Vector3f;

public class Scene {

  final ArrayDeque<GameObject> meshes;
  final ArrayDeque<GameObject> lights;
  final Vector3f ambientColor;
  final float ambientIntensity;
  int directionalCount, pointCount, spotCount;

  public Scene() {
    meshes = new ArrayDeque<>();
    lights = new ArrayDeque<>();

    ambientColor = new Vector3f(1.0f);
    ambientIntensity = 0.1f;

    directionalCount = 0;
    pointCount = 0;
    spotCount = 0;
  }

  public void add(GameObject object) {
    switch (object.type) {
      case OBJECT_TYPE_MESH:
        meshes.push(object);
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

    PhongShader.shader.bind();
    PhongShader.shader.setUniformVec3f("ambient.color", ambientColor);
    PhongShader.shader.setUniformFloat("ambient.intensity", ambientIntensity);

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
          PhongShader.shader.setUniformVec3f("directional[" + dirCount + "].color", dir.color);
          PhongShader.shader.setUniformFloat(
              "directional[" + dirCount + "].intensity", dir.intensity);
          PhongShader.shader.setUniformVec3f(
              "directional[" + dirCount + "].direction", dir.direction);

          dirCount++;
          break;

        case LIGHT_TYPE_POINTLIGHT:
          PointLight poi = (PointLight) temp;

          PhongShader.shader.setUniformVec3f(
              "pointLights[" + poiCount + "].position", poi.transform.position);
          PhongShader.shader.setUniformVec3f("pointLights[" + poiCount + "].color", poi.color);
          PhongShader.shader.setUniformFloat(
              "pointLights[" + poiCount + "].intensity", poi.intensity);
          PhongShader.shader.setUniformFloat(
              "pointLights[" + poiCount + "].linear", poi.linearTerm);
          PhongShader.shader.setUniformFloat(
              "pointLights[" + poiCount + "].quadratic", poi.quadraticTerm);

          poiCount++;
          break;

        case LIGHT_TYPE_SPOTLIGHT:
          SpotLight spo = (SpotLight) temp;

          PhongShader.shader.setUniformVec3f(
              "spotLights[" + spoCount + "].position", spo.transform.position);
          PhongShader.shader.setUniformVec3f(
              "spotLights[" + spoCount + "].direction", spo.direction);
          PhongShader.shader.setUniformVec3f("spotLights[" + spoCount + "].color", spo.color);
          PhongShader.shader.setUniformFloat(
              "spotLights[" + spoCount + "].intensity", spo.intensity);
          PhongShader.shader.setUniformFloat("spotLights[" + spoCount + "].linear", spo.linearTerm);
          PhongShader.shader.setUniformFloat(
              "spotLights[" + spoCount + "].quadratic", spo.quadraticTerm);
          PhongShader.shader.setUniformFloat("spotLights[" + spoCount + "].cutOff", spo.mCutOff);

          spoCount++;
          break;
      }
    }
  }

  public void render() {

    updateLighting();

    for (GameObject mesh : meshes) {
      ((Mesh) mesh).render();
    }
  }
}
