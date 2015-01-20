package robowars.graphics.model

import javax.media.opengl.GL4
import javax.media.opengl.GL._


class SimpleMaterial(implicit gl: GL4)
extends Material[VertexXY, EmptyVertex.type](
  gl = gl,
  vsPath = "src/main/shaders/basic_vs.glsl",
  fsPath = "src/main/shaders/basic_fs.glsl",
  "vertexPos",
  None,
  GL_DEPTH_TEST
)
