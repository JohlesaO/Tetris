/*
* Search for ADDED to find our added code or search
* specificaly...
*
* "MOVING UP"
* "BOMB"
* "POINTS"
* "SAVING PEICE"
* "NEW KEYS"
* "INSTRUCTIONS"
*
*/

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Board extends JPanel {

    private final int BOARD_WIDTH = 10; 
    private final int BOARD_HEIGHT = 22;
    private final int PERIOD_INTERVAL = 300;
    
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0; // score
    private int curX = 0;
    private int curY = 0;
    private JLabel statusbar;
    private Shape curPiece;
    private Tetrominoe[] board;

    ////// ADDED VARIABLES
    private final int BOMB_ROWS = 2; // The number of rows needed to get a bomb
    private int totalLinesRemoved = 0; // not the score
    private String bombAvailable = " "; //will appear on the status bar
    private boolean playerStillHasBomb = false;
    private int justRemoved = 0; //to make sure that another bomb will not be made in the same row removed
    private boolean bombInPlay = false;//turns true when a bomb is dropping
    //////

    public Board(Tetris parent) {

        initBoard(parent);
    }

    private void initBoard(Tetris parent) {

        setFocusable(true);
        statusbar = parent.getStatusBar();
        addKeyListener(new TAdapter());
        setBackground(Color.white);
    }

    private int squareWidth() {

        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {

        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    /* returns the tetrominoe shape at the specified x, y coordinate*/
    private Tetrominoe shapeAt(int x, int y) {

        return board[(y * BOARD_WIDTH) + x];
    }

    void start() {

          curPiece = new Shape();
          board = new Tetrominoe[BOARD_WIDTH *  BOARD_HEIGHT];

          clearBoard();
          newPiece();
          timer = new Timer(PERIOD_INTERVAL,new  GameCycle());
          timer.start();
          instructions();
    }

    private void pause() {

        isPaused = !isPaused;

        if (isPaused) {

            statusbar.setText("paused");
        } else {

            statusbar.setText(String.valueOf(numLinesRemoved) + bombAvailable); // score
        }

        repaint();
    }

    
    @Override
    public void paintComponent(Graphics g) {

        // super is the JPanel
        // we might want to look at JPanel
        super.paintComponent(g);
        doDrawing(g);
    }

    /* where most of the graphic happen */
    //display
    private void doDrawing(Graphics g) {
        var size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
          //first section here colors all of the already dropped pieces
        for (int i = 0; i < BOARD_HEIGHT; i++) {

            for (int j = 0; j < BOARD_WIDTH; j++) {

                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Tetrominoe.NoShape) {

                    drawSquare(g, j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
                }
            }
        }
        //just the newly dropping piece
        if (curPiece.getShape() != Tetrominoe.NoShape) {

            for (int i = 0; i < 4; i++) {

                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);

                drawSquare(g, x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape());
            }
        }
    }

    // THIS METHOD DROPS A PEICE TO THE BOTTOM 
    // only used when space is pressed
    private void dropDown() {

        int newY = curY;

        while (newY > 0) {

            if (!tryMove(curPiece, curX, newY - 1)) {

                break;
            }

            newY--;

        }

        pieceDropped();
    }

    // THIS METHOD DROPS EACH PIECE DOWN ONE LINE
    private void oneLineDown(){

        if (!tryMove(curPiece, curX, curY - 1 )) {

              pieceDropped();
        }
    }


    ////// ADDED
    // METHODS FOR "MOVING UP"

    /* checks if the move up 5 lines is possible*/
    private void fiveLinesUp(){

      if(!tryMove(curPiece, curX, curY + 5)){
        // move the peice up 5 lines if possible
        for(int i = 0; i < 5; i++){

          pieceLifted();
        }
      }
    }

    private void pieceLifted(){//lifts the piece 5 times instead of drops. Just press 'S'.

      for(int i = 0; i<4; i++){//goes through all 4 squares belonging to the piece

        int x = curX + curPiece.x(i);
        int y = curY + curPiece.y(i);
        board[(y * BOARD_WIDTH) + x] = curPiece.getShape();

      }
      
      removeFullLines();
      
      if (!isFallingFinished) {

            newPiece();
      }
    }

    // END OF "MOVING UP" METHODS
    //////

    private void clearBoard() {

        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {

            board[i] = Tetrominoe.NoShape;
        }
    }

    /* this method is called when the piece has been dropped to do all the clean up afterwards*/
    private void pieceDropped() {

        for (int i = 0; i < 4; i++) {

            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
          if(bombInPlay){
            bombInPlay = false;
            explode();
          }
          newPiece();
        }
    }

    ////// ADDED
    // METHODS FOR THE "BOMB"

      /*
      An indicater appears it the status bar, then you can detonate the bomb by pressing b. This means that you will now have a bomb "piece" in play. You can move the bomb piece like any game piece, and choose where to detonate.

      The area of detonation will have a cavity with the shape:
                  * * * * * *
                  * * * * * *
                    * * * *

      Every 5 rows that a person removes, a bomb can be activated. Multiple bombs cannot be saved.

      A notification in the statusbar will appear when the player can use a bomb, but a notification will not appear more than once if the player already has a bomb to use.
      */

    // checks if a bomb should be available and does so if true
    private void bomb(){ // called in removeFullLines()

      if(justRemoved != numLinesRemoved){

        //every 5 lines removed, make a bomb a
        if (totalLinesRemoved % BOMB_ROWS == 0 && totalLinesRemoved != 0){

          justRemoved = numLinesRemoved; // used to make sure someone does not play a bomb twice
        
          bombAvailable = " Click 'B' to detonate!"; 
          statusbar.setText(String.valueOf(numLinesRemoved)  + bombAvailable);
          // the message apears in the status bar to notify the player that a bomb is available to play.
        }
      }
    }

    private void detonateBomb(){ //called when b is pressed
    
          if(bombAvailable.equals(" Click 'B' to detonate!")){ // checks if a bomb is available

            bombAvailable = " "; 
            statusbar.setText(String.valueOf(numLinesRemoved) + bombAvailable); // removes the message in the statusbar

            bombInPlay = true; // and puts the bomb into play
            newPiece(); // creates the bomb
          }
    }

    // revoves peices around the bomb to make the aperiance of an explosion
    private void explode(){
      /* removes the peices in the pattern:
            * * * * * *
            * * * * * *
              * * * *
      */

      //find the maximum rows that can be taken out depending on distance of bomb to ground
      int k = -1;
      for(int i = 2; i <= 4; i++){
        if(curY - i >= 0){
          k = i;
        }
      }

      // * * * * *
      // * * * * *
      for(int i = 2; i <= k; i++){//goes down
        int a = curY - i;
        int b = curX - 2;
        if(i == 4){
          // * * * *
          b = curX - 1;
          while(b <= curX + 2){//for a span of 4 squares
            board[(a * BOARD_WIDTH) + b] =   Tetrominoe.NoShape;//remove them
            b++;
          }
        }else{
          while(b <= curX + 3){//goes through every piece in the range of the top rows
            board[(a * BOARD_WIDTH) + b] =   Tetrominoe.NoShape;
            b++;
          }
        } 
      }

        getRidOfBomb(curX, curY -1);
    }

    private void getRidOfBomb(int a, int b){//removes each square in the bomb piece.
            board[(b * BOARD_WIDTH) + a] =      Tetrominoe.NoShape;//lower left corner
            int c = a;
            int d = b + 1;
            board[(d * BOARD_WIDTH) + c] =      Tetrominoe.NoShape;//lower right corner
            int e = a + 1;
            int f = b + 1;
            board[(f * BOARD_WIDTH) + e] =      Tetrominoe.NoShape;//upper right corner
            int g = a + 1;
            int h = b;
            board[(h * BOARD_WIDTH) + g] =      Tetrominoe.NoShape;//upper left corner
    }
    // END OF the "BOMB" METHODS
    //////


    /* Creates a new peice at the top of the board */
    private void newPiece() {
        //// ADDED
        if(bombInPlay){
          curPiece.setBombShape(); //sets shape to square
        ////
        } else{
          curPiece.setRandomShape();
          // Creates a new random shape
        }
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        // puts the peice at the top middle of the board

        if (!tryMove(curPiece, curX, curY)) {

          // If you can't move the peice, end the game
            curPiece.setShape(Tetrominoe.NoShape);
            timer.stop();

            var msg = String.format("Game over. Score: %d", numLinesRemoved);
            statusbar.setText(msg);
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {

        for (int i = 0; i < 4; i++) {
          
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            // Checking for out of bounds
            if (x < 0 || x >= BOARD_WIDTH || y < 0) {

              return false;
            }
            if (shapeAt(x, y) != Tetrominoe.NoShape) {

                return false;
            }
            if (y >= BOARD_HEIGHT){

              newY = BOARD_HEIGHT;
            }
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;

        repaint();

        return true;
    }

    /* removes a line that is complete*/
    private void removeFullLines() {

        int numFullLines = 0;

        // loops through the rows from top to bottom
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {

            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; j++) {

                // if it finds an empty spot, the line is not full
                if (shapeAt(j, i) == Tetrominoe.NoShape) {

                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {

                numFullLines++;

                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }
            }
        }

        if (numFullLines > 0) {

          ////// ADDED
          // CODE FOR THE DIFFERENT "POINTS"

          if(numFullLines > 1){ // if more than one line is completed at a time
          numLinesRemoved += (numFullLines * 2); // score 
          }
          // And now you get 2 bonus points for every row in addition to the normal points

          ////// END OF THE "POINTS" CODE

          numLinesRemoved += numFullLines; // score
          totalLinesRemoved += numFullLines; // not the score
          statusbar.setText(String.valueOf(numLinesRemoved) + bombAvailable); // score
          isFallingFinished = true;
          curPiece.setShape(Tetrominoe.NoShape);
        }

        ////// ADDED
        bomb(); // calls bomb() to see if a bomb should be made available
        ////// END
    }

    ////// ADDED
    // METHODS FOR "SAVING PEICE"

    private void swap(){//saves shape data to a variable in Shape.java. A method in another class does all the swapping. Press 'H' to swap.

      if(curPiece.savedIsNull()){
        curPiece.setSavedShape();
        newPiece();
      }else{
        curPiece.setSavedShape();
      }
    }

    ////// END OF "SAVING PEICE"

    // method draws in the black lines for each square in the tetrominoe
    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape) {

        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };
        
        ////// ADDED
        // CODE FOR "BOMB"
        if(bombInPlay){ // if there is a bomb in play

          var color = colors[0]; // then everything becomes black to make it obvious that there is a bomb in play
  
          g.setColor(color);
          g.fillRect(x + 1, y + 1, squareWidth() - 2,   squareHeight() - 2);
        ////// END OF CODE FOR "BOMB"

        }else{ //normal pieces
 
          var color = colors[shape.ordinal()];
 
          g.setColor(color);
          g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight()   - 2);
    
          g.setColor(color.brighter());
          g.drawLine(x, y + squareHeight() - 1, x, y);
          g.drawLine(x, y, x + squareWidth() - 1, y);
    
          g.setColor(color.darker());
          g.drawLine(x + 1, y + squareHeight() - 1,
                  x + squareWidth() - 1, y + squareHeight() - 1);
          g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
               x + squareWidth() - 1, y + 1);
        }
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            doGameCycle();
        }
    }

    private void doGameCycle() {

        update();
        repaint();
    }

    private void update() {

        if (isPaused) {

            return;
        }

        if (isFallingFinished) {

            isFallingFinished = false;
            newPiece();
        } else {

            oneLineDown();
        }
    }

    ////// ADDED "INSTRUCTIONS"
    private void instructions(){

      System.out.println("");

      System.out.println("These are the instructions for the use of this version of Tetris.");
      
      // ORIGINAL KEYS
      System.out.println("Use the left and right arrow keys to move the peice left and right.");
      System.out.println("Use the up and down arrow keys to rotate the peice.");
      System.out.println("Press 'p' to pause the game.");
      System.out.println("Press space to drop the peice all the way down.");
      System.out.println("Press 'd' to move the peice down one line.");
      
      // ADDED KEYS
      System.out.println("Press 's' to move the peice up 5 line/ slow it down.");
      System.out.println("Press 'h' to hold your current peice and/ or switch it with the currently held peice.");
      System.out.println("Press 'b' to activate a bomb when the message apears for you to do so.");
      
      
    }
    ////// END OF "INSTRUCTIONS" CODE

    // CLASS FOR KEYBOARD IMPUT
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            if (curPiece.getShape() == Tetrominoe.NoShape) {

                return;
            }

            int keycode = e.getKeyCode();

            // Java 12 switch expressions
            if(keycode == KeyEvent.VK_P)
            {
              pause();
            }
            else if(keycode == KeyEvent.VK_LEFT)
            {
              tryMove(curPiece, curX - 1, curY);
            }
            else if(keycode == KeyEvent.VK_RIGHT)
            {
              tryMove(curPiece, curX + 1, curY);
            }
            else if(keycode == KeyEvent.VK_DOWN)
            {
              tryMove(curPiece.rotateRight(), curX, curY);
            }
            else if(keycode == KeyEvent.VK_UP)
            {
              tryMove(curPiece.rotateLeft(), curX, curY);
            }
            else if(keycode == KeyEvent.VK_SPACE)
            {
              dropDown();
            }
            else if(keycode == KeyEvent.VK_D)
            {
              oneLineDown();
            } 
            ////// ADDED
            // CODE FOR "NEW KEYS"
            else if(keycode == KeyEvent.VK_S)
            // 'S' for slow
            {
              fiveLinesUp(); // moves peice up
            }
            else if(keycode == KeyEvent.VK_H)
            // 'H' for hold
            {
              swap(); // swaps the saved peice
              // look at shape.java for explanation
            }
            else if(keycode == KeyEvent.VK_B)
            // 'B' for bomb
            {
              detonateBomb(); // activates bomb
            }
            ////// END OF "NEW KEYS" CODE
        }
    }
}