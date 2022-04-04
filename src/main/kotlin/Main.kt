package indigo

fun main() {
    val packOfCards = Player.GameDeck.newDeck()
    val humanDeck = mutableListOf<Card>()
    val pcDeck = mutableListOf<Card>()
    val humanWinCards = mutableListOf<Card>()
    val pcWinCards = mutableListOf<Card>()
    val tableCards = mutableListOf<Card>()
    val human = Player(humanDeck, humanWinCards, "Player")
    val pc = Player(pcDeck, pcWinCards, "Computer")
    gameInit(human, pc, packOfCards, tableCards)
    val turn = gameFirstPrompt()
    if (turn == 1) human.first = true else pc.first = true
    print("Initial cards on the table: ")
    println(tableCards.joinToString(" "))
    gamePlay(human, pc, packOfCards, tableCards, turn)
    print("Game Over")
}

/* Initializing variables and game environment  */
fun gameInit(
    human: Player,
    pc: Player,
    packOfCards: MutableList<Card>,
    tableCards: MutableList<Card>,
) {
    tableCards.addAll(Player.GameDeck.transToTableCards(packOfCards))
    human.fillDeck(packOfCards)
    pc.fillDeck(packOfCards)
}

/* First dialogue function */
fun gameFirstPrompt(): Int {
    println("Indigo Card Game")
    while (true) {
        println("Play first?")
        val answer = readln()
        if (answer.contains("yes")) {
            return 1
        } else if (answer.contains("no")) {
            return 2
        }
    }
}
/* Gameplay processing function */
fun gamePlay(
    human: Player,
    pc: Player,
    packOfCards: MutableList<Card>,
    tableCards: MutableList<Card>, firstMove: Int
) {
    var _firstMove = firstMove
    var choice: String
    var currentCard:Card?

    while (human.winCards.size + pc.winCards.size + tableCards.size < 52) {
        if (tableCards.size == 0) {
            println()
            println("No cards on the table")
        } else {
            println()
            println("${tableCards.size} cards on the table, and the top card is ${tableCards.last()}")
        }
        if (_firstMove == 1) { /* Human's move starts here */
            print("Cards in hand: ")
            choice = humanMovePromptExecution(human.inHand)

            if (choice == "exit") return
            else {
                currentCard = human.inHand[choice.toInt() - 1]
                gameMove(human, pc, tableCards, packOfCards, currentCard )
                _firstMove++
            }
        } else { /* PC's move starts here */
            println(pc.inHand.joinToString(" "))
            currentCard = if (tableCards.isEmpty()) {
                emptyMove(pc.inHand)
            } else {
                pcMove(pc.inHand,tableCards)
            }
            println("Computer plays $currentCard")
            gameMove(pc, human, tableCards, packOfCards, currentCard!!)
            _firstMove--
        }
    }
    /* Start of processing of final calculations in the end of the game*/
    if (tableCards.size > 0) {
        println()
        println("${tableCards.size} cards on the table, and the top card is ${tableCards.last()}")
        if (human.lastWin) {
            human.winCards.addAll(tableCards)
        } else if (pc.lastWin) {
            pc.winCards.addAll(tableCards)
        } else if (human.first) {
            human.winCards.addAll(tableCards)
        }
    }   else {
        println()
        println("No cards on the table")
        pc.winCards.addAll(tableCards)
    }
    scoreCalc(human, pc)
    if (human.winCards.size == pc.winCards.size && human.first) {
        human.score += 3
    } else if (human.winCards.size == pc.winCards.size && pc.first) {
        pc.score += 3
    } else if (human.winCards.size > pc.winCards.size) {
        human.score += 3
    } else pc.score += 3
    printInGameScore(human, pc)
}

/* Processing of a Player's move */
fun gameMove(
    mover: Player,
    opposite: Player,
    tableCards: MutableList<Card>,
    packOfCards: MutableList<Card>,
    card: Card,
) {
    if (tableCards.size > 0 && winCardCheck(tableCards,card)) {
        println("${mover.name} wins cards")
        mover.lastWin = true
        opposite.lastWin = false
        Player.GameDeck.ifWinTransferCards(mover.inHand, mover.winCards, tableCards, card)
        scoreCalc(mover, opposite)
        if (mover.winCards.size + opposite.winCards.size <= 52) {
            printInGameScore(mover, opposite)
        }
    } else {
        tableCards.add(card)
        mover.removeCard(card)
    }
    if (mover.inHand.size == 0 && packOfCards.size != 0) {
        mover.fillDeck(packOfCards)
    }
}

/* Checking if player's card wins */
fun winCardCheck(tableCards: MutableList<Card>, card: Card): Boolean {
    return card.suite() == tableCards.last().suite() ||
            card.rank() == tableCards.last().rank()
}

/* Processing of human's move choice */
fun humanMovePromptExecution(human: MutableList<Card>): String {
    var checker = 0
    print(buildString {
        for (i in 1..human.size) {
            append("$i)${human[i - 1]} ")
        }
        append("\n")
    })
    while (checker == 0) {
        println("Choose a card to play (1-${human.size}):")
        val choice: String? = readlnOrNull()
        if (choice != null) {
            try {
                if (choice.contains("exit")) {
                    checker++
                    return "exit"
                } else if (choice.toInt() <= human.size &&
                    choice.toInt() > 0
                ) {
                    checker++
                    return choice
                }
            } catch (e: NumberFormatException) {
            }
        }
    }
    return ""
}

/*In game score calculation */
fun scoreCalc(player1: Player, player2: Player) {
    player1.scoreCard(player1)
    player2.scoreCard(player2)
}

/* Printing the score */
fun printInGameScore(player1: Player, player2: Player) {
    if (player1.name == "Player") {
        println("Score: Player ${player1.score} - Computer ${player2.score}")
        println("Cards: Player ${player1.winCards.size} - Computer ${player2.winCards.size}")
    } else {
        println("Score: Player ${player2.score} - Computer ${player1.score}")
        println("Cards: Player ${player2.winCards.size} - Computer ${player1.winCards.size}")
    }
}

/* Choice of a card to move when there are no cards on the table */
private fun emptyMove(inHand: MutableList<Card>): Card {
    val move: Card?
    val suites = inHand.groupBy { it.suite() }.toMutableMap().filterValues { it.size > 1 }
    val ranks = inHand.groupBy { it.rank() }.toMutableMap().filterValues { it.size > 1 }
    return when {
        suites.isNotEmpty() -> {
            move = (suites.values).toMutableList().random().random()
            move
        }
        ranks.isNotEmpty() && suites.isEmpty() -> {
            move = (ranks.values).toMutableList().random().random()
            move
        }
        else -> inHand.random()
    }
}

/* Choice of a suite or rank to move */
private fun suiteOrRankSearch(inHand: MutableList<Card>, suite: String, rank: String): Card? {
    val suitList:MutableList<Card>? = mutableListOf()
    val rankList: MutableList<Card>? = mutableListOf()
    suitList?.addAll(inHand)
    rankList?.addAll(inHand)
    suitList?.removeIf { it.suite() != suite } ?: suitList?.isEmpty()
    rankList?.removeIf { it.rank() != rank } ?: rankList?.isEmpty()
    if (suitList?.size!! > 0 && suitList.size >= rankList?.size!!) {
        return suitList.random()
    } else if (rankList?.size!! > 0) {
        return rankList.random()
    }
    return inHand.random()
}

/* PC's putting a card on the table */
fun pcMove(inHand: MutableList<Card>, tableCards: MutableList<Card>): Card? {
    return if (inHand.any { it.suite() == tableCards.lastOrNull()?.suite() } || inHand.any {
            it.rank() == tableCards.lastOrNull()?.rank()
        }) {
        suiteOrRankSearch(inHand, tableCards.last().suite(), tableCards.last().rank())
    } else {
        emptyMove(inHand)

    }
}
class Card(private val rank: String, private val suite: String) {
    override fun toString(): String {
        return "$rank$suite"
    }
    fun suite(): String {
        return suite
    }
    fun rank(): String {
        return rank
    }
}

open class Player(
    val inHand: MutableList<Card>,
    val winCards: MutableList<Card>,
    open val name: String,
    var lastWin: Boolean = false,
    var first: Boolean = false
) {
    var score = 0

    /* Player draws cards from the deck */
    fun fillDeck(packOfCards: MutableList<Card>): MutableList<Card> {
        inHand.addAll(GameDeck.transferCards(packOfCards))
        return inHand
    }

    /* score calculation */
    fun scoreCard(player: Player) {
        var counter = 0
        for (it in player.winCards) {
            if (it.rank() == "A" || it.rank() == "Q" || it.rank() == "K"
                || it.rank() == "J" || it.rank() == "10"
            )
                counter++
        }
        player.score = counter
    }

    fun removeCard(card: Card) {
        inHand.removeIf { it == card }
    }


    object GameDeck {
        private val ranks = mutableListOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        private val suites = mutableListOf("♦", "♥", "♠", "♣")
        private lateinit var packOfCards: MutableList<Card>

        /*Transferring to the table 4 cards at the beginning of the game*/
        fun transToTableCards(packOfCards: MutableList<Card>): MutableList<Card> {
            val trans = packOfCards.slice(0..3)
            packOfCards.removeAll(packOfCards.slice(0..3))
            return trans.toMutableList()
        }

        /* transferring cards from the game deck to the players' decks */
        fun transferCards(
            packOfCards: MutableList<Card>
        ): MutableList<Card> {
            val transfercards = mutableListOf<Card>()
            transfercards.addAll(packOfCards.slice(0..5))
            packOfCards.removeAll(packOfCards.slice(0..5))
            return transfercards

        }

        /* Generating of the new shuffled game deck */
        fun newDeck(): MutableList<Card> {
            packOfCards = mutableListOf()
            for (suite in suites) {
                for (rank in ranks) {
                    packOfCards.add(Card(rank, suite))
                }
            }
            return packOfCards.shuffled().toMutableList()
        }

        /* transferring cards from table to winner */
        fun ifWinTransferCards(
            inHand: MutableList<Card>,
            winCards: MutableList<Card>,
            tableCards: MutableList<Card>,
            card: Card
        ) {
            winCards.addAll(tableCards + card)
            tableCards.clear()
            inHand.removeIf { it == card }
        }
    }
}