package bobby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ScotlandYard implements Runnable {

	/*
		this is a wrapper class for the game.
		It just loops, and runs game after game
	*/

  public int port;
  public int gamenumber;
  private PrintWriter out;
  private BufferedReader in;
  private Thread moderatorThread;

  public ScotlandYard(int port) {
    this.port = port;
    this.gamenumber = 0;
  }

  @Override public void run() {
    while (true) {
      Thread tau = new Thread(new ScotlandYardGame(this.port, this.gamenumber));
      tau.start();
      try {
        tau.join();
      } catch (InterruptedException e) {
        return;
      }
      this.gamenumber++;
    }
  }

  public class ScotlandYardGame implements Runnable {
    private Board board;
    private ServerSocket server;
    public int port;
    public int gamenumber;
    private ExecutorService threadPool;

    public ScotlandYardGame(int port, int gamenumber) {
      this.port = port;
      this.board = new Board();
      this.gamenumber = gamenumber;
      try {
        this.server = new ServerSocket(port);
        System.out.println(String.format("Game %d:%d on", port, gamenumber));
        server.setSoTimeout(50000);
      } catch (IOException i) {
        System.out.println("Error {}  " + i);
        return;
      }
      this.threadPool = Executors.newFixedThreadPool(10);
    }

    @Override public void run() {

      try {

        //INITIALISATION: get the game going

        Socket socket = this.server.accept();
        boolean fugitiveIn = false;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String feedback;
				/*
				listen for a client to play fugitive, and spawn the moderator.
				
				here, it is actually ok to edit this.board.dead, because the game hasn't begun
				*/
        do {
          this.board.dead = false;
          if ((feedback = in.readLine()) != null) {
            System.out.println(feedback);
            fugitiveIn = feedback.split(" ")[3].equals("Fugitive");
          }
          //          this.board.embryo = false;
        } while (!fugitiveIn);

        System.out.println(this.gamenumber);

        // Spawn a thread to run the Fugitive
        Thread fugitiveThread =
            new Thread(new ServerThread(board, -1, this.server.accept(), port, gamenumber));
        fugitiveThread.start();

        // Spawn the moderator


        moderatorThread = new Thread(new Moderator(this.board));
        moderatorThread.start();


        while (true) {
					/*
					listen on the server, accept connections
					if there is a timeout, check that the game is still going on, and then listen again!
					*/
          try {
            socket = this.server.accept();
          } catch (SocketTimeoutException t) {
            if (!this.board.dead) {
              socket = this.server.accept();
            }
            continue;
          }

					/*
					acquire thread info lock, and decide whether you can serve the connection at this moment,

					if you can't, drop connection (game full, game dead), continue, or break.

					if you can, spawn a thread, assign an ID, increment the totalThreads

					don't forget to release lock when done!
					*/

          if (this.board.dead) {
            break;
          }


          this.board.threadInfoProtector.acquire();
          int id = this.board.getAvailableID();
          if (id == -1) {
            continue;
          } else {
            Thread tau = new Thread(new ServerThread(board, id, socket, port, gamenumber));
            tau.start();
            this.board.totalThreads++;
          }
          this.board.threadInfoProtector.release();
        }

			/*
				reap the moderator thread, close the server,

				kill threadPool (Careless Whispers BGM stops)
				*/
        in.close();
        out.close();
        socket.close();
        moderatorThread.interrupt();
        this.threadPool.awaitTermination(1, TimeUnit.DAYS);
        System.out.println(String.format("Game %d:%d Over", this.port, this.gamenumber));
        return;
      } catch (InterruptedException ex) {
        System.err.println("An InterruptedException was caught: " + ex.getMessage());
        ex.printStackTrace();
        return;
      } catch (IOException i) {
        return;
      }

    }


  }

  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++) {
      int port = Integer.parseInt(args[i]);
      Thread tau = new Thread(new ScotlandYard(port));
      tau.start();
    }
  }
}
