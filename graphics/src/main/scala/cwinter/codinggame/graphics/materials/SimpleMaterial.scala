package cwinter.codinggame.graphics.materials

import javax.media.opengl.GL._
import javax.media.opengl.GL4

import cwinter.codinggame.util.maths.{EmptyVertex, VertexXY}


class SimpleMaterial(implicit gl: GL4)
extends Material[VertexXY, EmptyVertex.type, Unit](
  gl = gl,
  vsPath = "basic_vs.glsl",
  fsPath = "basic_fs.glsl",
  "vertexPos",
  None,
  GL_DEPTH_TEST
)
