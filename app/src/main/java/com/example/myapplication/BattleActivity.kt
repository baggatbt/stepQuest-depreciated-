package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.main.*
import com.example.myapplication.MainActivity
import android.widget.Toast
import com.example.myapplication.ui.main.EnemyType
import com.example.myapplication.ui.main.Enemy




import kotlin.random.Random


class BattleActivity : AppCompatActivity(), OnEnemyHealthChangedListener, OnEnemyClickListener,TurnOrderUpdateListener,Battle.TurnOrderUpdateCallback {

    private lateinit var abilityOneButton: Button
    private lateinit var basicAttackButton: Button
    private lateinit var enemyAdapter: EnemyAdapter
    private lateinit var enemiesRecyclerView: RecyclerView
    private lateinit var turnOrderAdapter: TurnOrderAdapter
    private lateinit var turnOrderRecyclerView: RecyclerView

    lateinit var battle: Battle
    var player = MainActivity.player
    private var selectedEnemy: Enemy? = null

    private fun updateSelectedEnemyUI() {
        // Update the UI to show the selected enemy
        for (i in 0 until enemiesRecyclerView.childCount) {
            val enemyViewHolder = enemiesRecyclerView.findViewHolderForAdapterPosition(i)
            if (enemyViewHolder is EnemyAdapter.EnemyViewHolder) {
                val enemyImageView =
                    enemyViewHolder.itemView.findViewById<ImageView>(R.id.enemyImageView)
                val targetedEnemyIcon =
                    enemyViewHolder.itemView.findViewById<ImageView>(R.id.targetedEnemyIcon)
                if (selectedEnemy == enemyViewHolder.enemy) {
                    enemyImageView.translationZ = 10f
                    targetedEnemyIcon.translationZ = 11f
                    targetedEnemyIcon.visibility = View.VISIBLE

                    // Center the targetedEnemyIcon over the targetedEnemyImageView
                    val targetWidth = enemyImageView.width
                    val targetHeight = enemyImageView.height
                    val targetX = enemyImageView.x + (targetWidth - targetedEnemyIcon.width) / 2f
                    val targetY = enemyImageView.y + (targetHeight - targetedEnemyIcon.height) / 2f
                    targetedEnemyIcon.setX(targetX)
                    targetedEnemyIcon.setY(targetY)
                } else {
                    enemyImageView.background = null
                    enemyImageView.translationZ = 0f
                    targetedEnemyIcon.translationZ = 0f
                    targetedEnemyIcon.visibility = View.INVISIBLE
                }
            }
        }
        // Clear the previous glow effect
        glowingTurnOrderIcon?.clearAnimation()
        glowingTurnOrderIcon?.alpha = 1.0f
        glowingTurnOrderIcon = null
        glowAnimation?.cancel()
        glowAnimation = null

        updateTurnOrderGlow()
    }



    // Called when the health of an enemy changes
    override fun onEnemyHealthChanged(enemy: Enemy) {
        // Update the RecyclerView to show the new health values
        (enemiesRecyclerView.adapter as? EnemyAdapter)?.notifyDataSetChanged()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        // Set up the RecyclerView containing the list of enemies
        setupEnemiesRecyclerView()

        turnOrderRecyclerView = findViewById(R.id.turnOrderRecyclerView)
        turnOrderAdapter = TurnOrderAdapter(listOf())
        turnOrderRecyclerView.adapter = turnOrderAdapter



        // Initialize and set up the ability buttons and basic attack button
        val abilityCardsLayout = LayoutInflater.from(this).inflate(R.layout.ability_cards, null)
        val basicAttackLayout = LayoutInflater.from(this).inflate(R.layout.basicattackbutton, null)
        val bottomLayout = findViewById<ConstraintLayout>(R.id.root)

        // Add constraints to position the ability cards at the bottom of the root layout
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        val params2 = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params2.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        abilityCardsLayout.layoutParams = params
        basicAttackLayout.layoutParams = params2

        bottomLayout.addView(abilityCardsLayout)
        bottomLayout.addView(basicAttackLayout)

        abilityOneButton = abilityCardsLayout.findViewById(R.id.ability_card_1)
        basicAttackButton = basicAttackLayout.findViewById(R.id.basicAttackButton)


        // Initialize the battle and start it with the first enemy in the list
        battle = Battle(this, this,this)
        battle.start(player, enemyAdapter.enemies, this) // Get the chosen skill
        updateTurnOrder()

    }

    // Sets up the enemies RecyclerView
    // Sets up the enemies RecyclerView
    private fun setupEnemiesRecyclerView() {
        enemiesRecyclerView = findViewById(R.id.enemiesRecyclerView)
        enemiesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val enemies =
            generateEnemyList() // Use the generateEnemyList() function to create a list of random enemies

        val adapter = EnemyAdapter(enemies, this)
        enemiesRecyclerView.adapter = adapter

        enemyAdapter = adapter
    }

    override fun onEnemyClick(enemy: Enemy) {
        selectedEnemy = enemy
        battle.selectedEnemy = enemy // Update the selected enemy in the Battle instance
        updateSelectedEnemyUI()
    }


    // Generates a random list of enemies based on the number of enemies specified
    private fun generateEnemyList(): List<Enemy> {
        val numberOfEnemies = 1
        val enemyList = mutableListOf<Enemy>()
        for (i in 1..numberOfEnemies) {
            val enemyType = EnemyType.values().random()
            enemyList.add(Enemy(enemyType))
        }
        return enemyList
    }

    override fun onTurnOrderUpdated(turnOrderItems: List<TurnOrderItem>) {
        val turnOrderAdapter = (turnOrderRecyclerView.adapter as TurnOrderAdapter)
        turnOrderAdapter.update(turnOrderItems)
        updateTurnOrderGlow()
    }


    override fun updateTurnOrder() {
        val turnOrderItems = battle.turnOrder.map { identifier ->
            val imageResource = if (identifier == "character") {
                R.drawable.sword3030 // Replace with the player's drawable resource
            } else {
                R.drawable.sword3030 // Replace with the common enemy's drawable resource
            }
            val speed = if (identifier == "character") {
                player.speed // Replace with the player's speed property
            } else {
                val enemyIndex = identifier.substring(5).toInt()
                enemyAdapter.enemies[enemyIndex].speed // Replace with the enemy's speed property
            }
            TurnOrderItem(
                id = identifier,
                imageResource = imageResource,
                speed = speed
            )
        }
        turnOrderAdapter.update(turnOrderItems)
    }

    private var glowingTurnOrderIcon: ImageView? = null
    private var glowAnimation: ObjectAnimator? = null


    private fun updateTurnOrderGlow() {
        for (i in 0 until turnOrderRecyclerView.childCount) {
            val turnOrderViewHolder = turnOrderRecyclerView.findViewHolderForAdapterPosition(i)
            if (turnOrderViewHolder is TurnOrderAdapter.ViewHolder) {
                val turnOrderIcon = turnOrderViewHolder.turnOrderIcon
                val turnOrderItem = turnOrderViewHolder.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?.let { position -> turnOrderAdapter.turnOrderItems[position] }
                if (turnOrderItem != null && turnOrderItem.id.startsWith("enemy") && turnOrderItem.id.substring(5).toInt() == enemyAdapter.enemies.indexOf(selectedEnemy)) {
                    // Make the turn order icon glow
                    glowAnimation?.cancel() // Cancel the previous animation if it's running
                    glowingTurnOrderIcon = turnOrderIcon
                    glowAnimation = ObjectAnimator.ofFloat(turnOrderIcon, "alpha", 0.5f, 1.0f)
                    glowAnimation?.duration = 1000
                    glowAnimation?.repeatCount = ObjectAnimator.INFINITE
                    glowAnimation?.repeatMode = ObjectAnimator.REVERSE
                    glowAnimation?.start()
                }
            }
        }
    }





}
