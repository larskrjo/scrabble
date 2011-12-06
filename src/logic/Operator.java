package logic;

import dictionary.Dictionary;
import dictionary.Direction;
import dictionary.Type;
import gui.GUI;

/**
 * Created by IntelliJ IDEA.
 * User: havard_normann
 * Date: 16.11.11
 * Time: 20.11
 * To change this template use File | Settings | File Templates.
 */
public class Operator {
    public Board board;
    public Dictionary dictionary;
    public Player playerA;
    public Player playerB;
    public Bag bag;
    public GUI gui;
    public Turn turn;

    public Operator() {
        this.board = new Board();
        this.dictionary = new Dictionary();
        this.bag = new Bag();
        this.playerA = new Player(this.bag.drawPlayerStacks());
        this.playerB = new Player(this.bag.drawPlayerStacks());
        this.gui = new GUI(this);
	    this.turn = Turn.PLAYER_A;
    }

    public String makeMove() {
	    String new_word = "--";
        String rack;
        boolean future;
        if (turn == Turn.PLAYER_A) {
            rack = this.playerA.getLetters().toString();
            future = true;
        } else {
            rack = this.playerB.getLetters().toString();
            future = false;
        }
        //System.out.println("Rack: " + rack);

        Placement placement = Intelligence.getPlacement(this.dictionary, this.board, rack, future);

        if(placement == null) {
            String rack_change = Intelligence.rackExchange(this.bag, rack);
            if (turn == Turn.PLAYER_A) {
                for (char letter : rack_change.toCharArray()) {
                    this.playerA.removeLetter(letter);
                    letter = this.bag.exchangeLetter(letter);
                    this.playerA.addLetter(letter);
                }
                this.playerA.newPass();
            } else {
                for (char letter : rack_change.toCharArray()) {
                    this.playerB.removeLetter(letter);
                    letter = this.bag.exchangeLetter(letter);
                    this.playerB.addLetter(letter);
                }
                this.playerB.newPass();
            }
        } else {
	        System.out.println("col: " + placement.getCol() + " row: " + placement.getRow() + " direction: " +
			        placement.getDirection());
            int score = this.board.computeScore(placement);
            System.out.println("Score: "+ score);
	        new_word = placement.getWord();
            this.board.addWord(placement);
            //System.out.println("The score for this word is: " +  score);
            if (turn == Turn.PLAYER_A) {
                this.playerA.clearPass();
                this.playerA.addScore(score);
                String usedRack = placement.getRack();
                System.out.println("Rack " + usedRack + ", word " + placement.getWord());
                this.playerA.removeWord(usedRack);
                while(!this.playerA.isRackFull() && this.bag.letters.size() != 0) {
                     this.playerA.addLetter(this.bag.drawLetter());
                }
            } else {
                this.playerB.clearPass();
                this.playerB.addScore(score);
                String usedRack = placement.getRack();
                //System.out.println("Rack " + usedRack + ", word " + placement.getWord());
                this.playerB.removeWord(usedRack);
                while(!this.playerB.isRackFull() && this.bag.letters.size() != 0) {
                     this.playerB.addLetter(this.bag.drawLetter());
                }
            }
        }
        System.out.println("Heuristic: " + Intelligence.placementFutureValue(this.dictionary, this.board, placement));
        changeTurn();
        this.gui.update();
	    return new_word;

    }

    public boolean endGame() {
        return this.playerA.passLimit() || this.playerB.passLimit();
    }

    public Player winner() {
        if (this.playerA.getScore() > this.playerB.getScore()) {
            return this.playerA;
        } else if(this.playerA.getScore() < this.playerB.getScore()) {
            return this.playerB;
        } else {
            return null;
        }
    }

    public String winnerToString() {
        if (winner() == this.playerA) {
            return "logic.Player A";
        } else if (winner() == this.playerB) {
            return "logic.Player B";
        } else {
            return "Draw";
        }
    }

    public void changeTurn() {
	    if(turn == Turn.PLAYER_A){
			turn = Turn.PLAYER_B;
	    }
	    else {
		    turn = Turn.PLAYER_A;
	    }
	    gui.update();
    }

    public static void main(String[] args){
        int average = 0;
        int A = 0;
        int B = 0;
        for (int j = 0; j < 20; j++) {
        Operator operator = new Operator();
        System.out.println("Original bag:\n" + operator.bag);
	    System.out.println("Bag length:\n" + operator.bag.letters.size());
        Placement placement = new Placement("test", 0, 0, Direction.HORIZONTAL);
        operator.board.addWord(placement);

        int i = 0;
        while(!operator.endGame()) {
	    //for(int i = 0; i < 10; i++){

            System.out.println(i++ + ", word for above info: " + operator.makeMove());
        }
        average += operator.winner().getScore();
        if (operator.winner() == operator.playerA) {
            A += 1;
        } else {
            B += 1;
        }
        //System.out.println("The winner is: " + operator.winnerToString() + " with a total score: " + operator.winner
		//	    ().getScore());

        }
        System.out.println("A vant: " + A + ", B vant: " + B + " med average " + (int)average/20);
    }
}
