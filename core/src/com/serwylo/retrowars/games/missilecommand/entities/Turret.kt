package com.serwylo.retrowars.games.missilecommand.entities

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class Turret(val position: Vector2) {

    companion object {
        const val WIDTH = 10f
        const val HEIGHT = 30f
    }

    fun render(camera: Camera, r: ShapeRenderer) {
        r.identity()
        r.projectionMatrix = camera.combined
        r.translate(position.x, position.y, 0f)

        r.begin(ShapeRenderer.ShapeType.Line)
        r.rect(-WIDTH / 2f, 0f, WIDTH, HEIGHT)
        r.end()

        r.identity()
    }

}