package com.serwylo.retrowars.games

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Actor
import com.serwylo.retrowars.RetrowarsGame
import com.serwylo.retrowars.net.Player
import com.serwylo.retrowars.net.RetrowarsClient
import com.serwylo.retrowars.scoring.saveHighScore
import com.serwylo.retrowars.ui.GameViewport
import com.serwylo.retrowars.ui.HUD

abstract class GameScreen(protected val game: RetrowarsGame, private val gameDetails: GameDetails, minWorldWidth: Float, maxWorldWidth: Float) : Screen {

    companion object {
        const val TAG = "GameScreen"
    }

    private val camera = OrthographicCamera()
    protected val viewport = GameViewport(minWorldWidth, maxWorldWidth, camera)

    private val hud: HUD

    protected val client = RetrowarsClient.get()

    init {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
        viewport.apply(true)
        hud = HUD(game.uiAssets)

        client?.listen(
            networkCloseListener = { wasGraceful -> game.showNetworkError(game, wasGraceful) },
            playerStatusChangedListener = { player, status -> handlePlayerStatusChange(player, status) },
            scoreBreakpointListener = { player, strength -> handleBreakpointChange(player, strength) }
        )
    }

    private fun handleBreakpointChange(player: Player, strength: Int) {
        Gdx.app.log(TAG, "HANDLING BREAKPOINT CHANGE")
        if (player.id == client?.me()?.id) {
            // TODO: Show visual feedback that we are attacking other players.
                Gdx.app.log(TAG, "BAILING")
            return
        }

        hud.showAttackFrom(player, strength)
        onReceiveDamage(strength)
    }

    private fun handlePlayerStatusChange(player: Player, status: String) {
        val client = this.client ?: return

        if (player.id != client.me()?.id) {
            return
        }

        if (status == Player.Status.dead) {
            Gdx.app.log(TAG, "Server has instructed us that we are in fact dead. We will honour this request and go to the end game screen.")
            endGame()
        }
    }

    protected fun endGame() {
        // TODO: Show end of game screen.
        if (client == null) {
            Gdx.app.log(RetrowarsGame.TAG, "Ending single player game... Recording high score and then loading game select menu.")
            saveHighScore(gameDetails, getScore())
            game.showGameSelectMenu()
        } else {
            Gdx.app.log(RetrowarsGame.TAG, "Ending multiplayer game... Off to the end-game lobby.")
            client.changeStatus(Player.Status.dead)
            game.showEndMultiplayerGame()
        }
    }

    protected abstract fun getScore(): Long
    protected abstract fun updateGame(delta: Float)
    protected abstract fun renderGame(camera: OrthographicCamera)

    /**
     * When another player performs well, we will receive a message to tell us to get handicaped in some way.
     * e.g. for Asteroids you may add more asteroids to the screen, in Missile Command you may add more missiles.
     * However it could also be more creative, perhaps in asteroids it spins your ship around randomly, or blows you off course.
     * Perhaps in missile command it changes missiles to zig-zag down to earth, etc.
     * It is up to the game to decide how to handicap the player in response.
     *
     * The [strength] of the attack indicates how much we should handicap the current user in response.
     * The default is a strength of 1, when the other player increments their score by a certain threshold.
     * If they do something which makes their score increment by 2 or 3 times this increment, then [strength] will be 2 or 3 respectively.
     */
    protected abstract fun onReceiveDamage(strength: Int)

    override fun render(delta: Float) {

        updateGame(delta)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        Gdx.graphics.gL20.glClearColor(0f, 0f, 0f, 1f)
        Gdx.graphics.gL20.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.renderIn {
            renderGame(camera)
        }

        hud.render(getScore(), delta)

        Gdx.gl.glDisable(GL20.GL_BLEND)

    }

    fun getInputProcessor() = hud.getInputProcessor()

    /**
     * Provided an actor here, it will be added to the HUD over the section of the screen
     * responsible for showing the game contents.
     */
    protected fun addGameOverlayToHUD(overlay: Actor) {
        hud.addGameOverlay(overlay)
    }

    protected fun addGameScoreToHUD(score: Actor) {
        hud.addGameScore(score)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        hud.resize(width, height)

        resizeViewport(viewport.worldWidth, viewport.worldHeight)
    }

    open fun resizeViewport(viewportWidth: Float, viewportHeight: Float) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
    }

    override fun dispose() {
    }
}
