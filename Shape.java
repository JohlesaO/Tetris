import java.util.Random;

public class Shape {

    private Tetrominoe pieceShape;
    private int[][] coords;

    //// ADDED
    private Tetrominoe savedPieceShape;
    ////

    public Shape() {

        coords = new int[4][2];
        setShape(Tetrominoe.NoShape);
    }
    
    ////// ADDED
    // "SAVING PEICE" METHODS

    void setSavedShape(){

      Tetrominoe temp = savedPieceShape;
      savedPieceShape = pieceShape;

      if(temp != null){

        pieceShape = temp;
        setShape(temp); 
      }
    }

    public boolean savedIsNull(){
      return savedPieceShape == null;
    }

    /*  this method isn't used
    void setShapeToNone(){
      Tetrominoe[] values = Tetrominoe.values();
      setShape(values[0]);
    }
    */

    // "BOMB" METHOD
    void setBombShape(){
      // code from setRandom()
      // called in newPeice()
      Tetrominoe[] values = Tetrominoe.values();
      setShape(values[5]);
    }
    
    ////// END

    void setShape(Tetrominoe shape) {

        int[][][] coordsTable = new int[][][]{
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, -1}, {0, 0}, {-1, 0}, {-1, 1}},
                {{0, -1}, {0, 0}, {1, 0}, {1, 1}},
                {{0, -1}, {0, 0}, {0, 1}, {0, 2}},
                {{-1, 0}, {0, 0}, {1, 0}, {0, 1}},
                {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
                {{-1, -1}, {0, -1}, {0, 0}, {0, 1}},
                {{1, -1}, {0, -1}, {0, 0}, {0, 1}}
        };

        for (int i = 0; i < 4; i++) {

            System.arraycopy(coordsTable[shape.ordinal()], 0, coords, 0, 4);
        }

        pieceShape = shape;
    }

    private void setX(int index, int x) {

        coords[index][0] = x;
    }

    private void setY(int index, int y) {

        coords[index][1] = y;
    }

    int x(int index) {

        return coords[index][0];
    }

    int y(int index) {

        return coords[index][1];
    }

    Tetrominoe getShape() {

        return pieceShape;   
    }

    void setRandomShape() {

        var r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;

        Tetrominoe[] values = Tetrominoe.values();
        setShape(values[x]);
        // 1: red ( _| )
        //        ( |  )
        // 2: green ( |_ )
        //          (   | )
        // 3: dark blue ( | )
        // 4: yellow ( _|_ )
        // 5: purple ( [] )
        // 6: light blue ( L )
        // 7: orange ( _| )

    }

    public int minX() {

        int m = coords[0][0];

        for (int i = 0; i < 4; i++) {

            m = Math.min(m, coords[i][0]);
        }

        return m;
    }


    int minY() {

        int m = coords[0][1];

        for (int i = 0; i < 4; i++) {

            m = Math.min(m, coords[i][1]);
        }

        return m;
    }

    Shape rotateLeft() {

        if (pieceShape == Tetrominoe.SquareShape) {

            return this;
        }

        var result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; i++) {

            result.setX(i, y(i));
            result.setY(i, -x(i));
        }

        return result;
    }

    Shape rotateRight() {

        if (pieceShape == Tetrominoe.SquareShape) {

            return this;
        }

        var result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; i++) {

            result.setX(i, -y(i));
            result.setY(i, x(i));
        }

        return result;
    }
}