package com.pixelrpg.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Central place for shader source creation/compilation.
 */
public final class ShaderFactory {
    private ShaderFactory() {
    }

    public static ShaderProgram createSaturationShader(String logTag) {
        ShaderProgram.pedantic = false;
        String vertex = "attribute vec4 a_position;\n"
                + "attribute vec4 a_color;\n"
                + "attribute vec2 a_texCoord0;\n"
                + "uniform mat4 u_projTrans;\n"
                + "varying vec4 v_color;\n"
                + "varying vec2 v_texCoords;\n"
                + "void main() {\n"
                + "  v_color = a_color;\n"
                + "  v_texCoords = a_texCoord0;\n"
                + "  gl_Position = u_projTrans * a_position;\n"
                + "}\n";
        String fragment = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_color;\n"
                + "varying vec2 v_texCoords;\n"
                + "uniform sampler2D u_texture;\n"
                + "uniform float u_saturation;\n"
                + "void main() {\n"
                + "  vec4 tex = texture2D(u_texture, v_texCoords);\n"
                + "  vec3 rgb = tex.rgb * v_color.rgb;\n"
                + "  float gray = dot(rgb, vec3(0.2126, 0.7152, 0.0722));\n"
                + "  vec3 saturated = mix(vec3(gray), rgb, u_saturation);\n"
                + "  gl_FragColor = vec4(saturated, tex.a * v_color.a);\n"
                + "}\n";
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) {
            Gdx.app.error(logTag, "Saturation shader compile failed: " + shader.getLog());
            shader.dispose();
            return null;
        }
        return shader;
    }

    public static ShaderProgram createLightAreaMaskShader(String logTag, int maxLightAreas) {
        ShaderProgram.pedantic = false;
        String vertex = "attribute vec4 a_position;\n"
                + "attribute vec4 a_color;\n"
                + "attribute vec2 a_texCoord0;\n"
                + "uniform mat4 u_projTrans;\n"
                + "varying vec4 v_color;\n"
                + "varying vec2 v_texCoords;\n"
                + "varying vec2 v_worldPos;\n"
                + "void main() {\n"
                + "  v_color = a_color;\n"
                + "  v_texCoords = a_texCoord0;\n"
                + "  v_worldPos = a_position.xy;\n"
                + "  gl_Position = u_projTrans * a_position;\n"
                + "}\n";
        String fragment = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_color;\n"
                + "varying vec2 v_texCoords;\n"
                + "varying vec2 v_worldPos;\n"
                + "uniform sampler2D u_texture;\n"
                + "uniform vec4 u_lightAreas[" + maxLightAreas + "];\n"
                + "uniform float u_lightTypes[" + maxLightAreas + "];\n"
                + "uniform float u_lightAreaCount;\n"
                + "float areaMask(vec2 p, vec4 a, float t) {\n"
                + "  float inRect = step(a.x, p.x) * step(p.x, a.x + a.z) * step(a.y, p.y) * step(p.y, a.y + a.w);\n"
                + "  vec2 halfSize = max(vec2(a.z * 0.5, a.w * 0.5), vec2(0.0001));\n"
                + "  vec2 center = vec2(a.x + halfSize.x, a.y + halfSize.y);\n"
                + "  vec2 d = (p - center) / halfSize;\n"
                + "  float ellipseDist = dot(d, d);\n"
                + "  float inEllipse = 1.0 - step(1.0, ellipseDist);\n"
                + "  float softEllipse = 1.0 - smoothstep(0.55, 1.0, ellipseDist);\n"
                + "  float isEllipse = step(0.5, t);\n"
                + "  float isSoftEllipse = step(1.5, t);\n"
                + "  float ellipseMask = mix(inEllipse, softEllipse, isSoftEllipse);\n"
                + "  return mix(inRect, ellipseMask, isEllipse);\n"
                + "}\n"
                + "void main() {\n"
                + "  vec4 tex = texture2D(u_texture, v_texCoords);\n"
                + "  vec3 rgb = tex.rgb * v_color.rgb;\n"
                + "  float mask = 0.0;\n"
                + "  for (int i = 0; i < " + maxLightAreas + "; i++) {\n"
                + "    float enabled = step(float(i), u_lightAreaCount - 0.5);\n"
                + "    mask = max(mask, areaMask(v_worldPos, u_lightAreas[i], u_lightTypes[i]) * enabled);\n"
                + "  }\n"
                + "  gl_FragColor = vec4(rgb, tex.a * v_color.a * mask);\n"
                + "}\n";
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) {
            Gdx.app.error(logTag, "Light area shader compile failed: " + shader.getLog());
            shader.dispose();
            return null;
        }
        return shader;
    }

    public static ShaderProgram createScreenBlurShader(String logTag) {
        ShaderProgram.pedantic = false;
        String vertex = "attribute vec4 a_position;\n"
                + "attribute vec4 a_color;\n"
                + "attribute vec2 a_texCoord0;\n"
                + "uniform mat4 u_projTrans;\n"
                + "varying vec4 v_color;\n"
                + "varying vec2 v_texCoords;\n"
                + "void main() {\n"
                + "  v_color = a_color;\n"
                + "  v_texCoords = a_texCoord0;\n"
                + "  gl_Position = u_projTrans * a_position;\n"
                + "}\n";
        String fragment = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_color;\n"
                + "varying vec2 v_texCoords;\n"
                + "uniform sampler2D u_texture;\n"
                + "uniform vec2 u_texelSize;\n"
                + "uniform float u_blurAmount;\n"
                + "void main() {\n"
                + "  vec2 offset = u_texelSize * (u_blurAmount * 6.0);\n"
                + "  vec4 color = texture2D(u_texture, v_texCoords) * 0.4;\n"
                + "  color += texture2D(u_texture, v_texCoords + vec2(offset.x, 0.0)) * 0.075;\n"
                + "  color += texture2D(u_texture, v_texCoords - vec2(offset.x, 0.0)) * 0.075;\n"
                + "  color += texture2D(u_texture, v_texCoords + vec2(0.0, offset.y)) * 0.075;\n"
                + "  color += texture2D(u_texture, v_texCoords - vec2(0.0, offset.y)) * 0.075;\n"
                + "  color += texture2D(u_texture, v_texCoords + vec2(offset.x, offset.y)) * 0.075;\n"
                + "  color += texture2D(u_texture, v_texCoords - vec2(offset.x, offset.y)) * 0.075;\n"
                + "  color += texture2D(u_texture, v_texCoords + vec2(offset.x, -offset.y)) * 0.075;\n"
                + "  color += texture2D(u_texture, v_texCoords - vec2(offset.x, -offset.y)) * 0.075;\n"
                + "  gl_FragColor = vec4(color.rgb * v_color.rgb, color.a * v_color.a);\n"
                + "}\n";
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) {
            Gdx.app.error(logTag, "Screen blur shader compile failed: " + shader.getLog());
            shader.dispose();
            return null;
        }
        return shader;
    }
}
