package cwinter.codecraft.graphics.materials

import com.jogamp.opengl.GL._
import com.jogamp.opengl.GL4

import cwinter.codecraft.util.maths.matrices.Matrix4x4
import cwinter.codecraft.util.maths.{ColorRGBA, VertexXYZ}


private[graphics] class TranslucentAdditive(implicit gl: GL4)
  extends JVMMaterial[VertexXYZ, ColorRGBA, Unit](
    gl = gl,
    vsPath = "xyz_rgba_vs.glsl",
    fsPath = "rgba_fs.glsl",
    "vertexPos",
    Some("vertexCol"),
    GL_BLEND
  ) {

  import gl._

  override def beforeDraw(projection: Matrix4x4): Unit = {
    super.beforeDraw(projection)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE)
  }
}
