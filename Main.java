//Olivia St. Marie && Johlesa Orm
//AP CS A Game Mod challenge 

// To find added code search for ADDED

/*
* So far with our changes you can "slow down" the current 
* piece by pressing 's'.
*
* The points system also awards 2 extra points per row
* each time a player fills 2+ rows at a time.
*
* You can also save a peice / switch to that saved peice
* by pressing 'h' (for hold).
*
* You can drop a bomb by pressing 'b' each time five rows 
* have been completed and removed.
*/

/*
* TO DO:
*
*/

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {

  public static void main(String[] args) {

    EventQueue.invokeLater(() -> {

        var game = new Tetris();
        game.setVisible(true);
    });
  }
}



class Tetris extends JFrame {

    private JLabel statusbar;

    public Tetris() {

        initUI();
    }

    private void initUI() {

        statusbar = new JLabel(" 0");
        add(statusbar, BorderLayout.SOUTH);
        
        var board = new Board(this);
        add(board);
        board.start();

        setTitle("Tetris");
        setSize(200,400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    JLabel getStatusBar() {

        return statusbar;
    }
}