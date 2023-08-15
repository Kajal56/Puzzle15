package bobby;

public class Moderator implements Runnable{
	private Board board;
	
	public Moderator(Board board){
		this.board = board;
	}

	@Override
	public void run(){
		while (true){
			try{
				/*acquire permits: 
				
				1) the moderator itself needs a permit to run, see Board
				2) one needs a permit to modify thread info

				*/
        this.board.moderatorEnabler.acquire();
        this.board.threadInfoProtector.acquire();
                                             


				/* 
				look at the thread info, and decide how many threads can be 
				permitted to play next round

				playingThreads: how many began last round
				quitThreads: how many quit in the last round
				totalThreads: how many are ready to play next round

				RECALL the invariant mentioned in Board.java

				T = P - Q + N

				P - Q is guaranteed to be non-negative.
				*/

				//base case
				
				if (this.board.embryo){
					this.board.registration.release();
					this.board.reentry.release();
					continue;
				}
				
				
				//find out how many newbies
				int newbies = this.board.quitThreads + this.board.totalThreads  - this.board.playingThreads ;


				/*
				If there are no threads at all, it means Game Over, and there are no 
				more new threads to "reap". dead has been set to true, then 
				the server won't spawn any more threads when it gets the lock.

				Thus, the moderator's job will be done, and this thread can terminate.
				As good practice, we will release the "lock" we held. 
				*/

				if(newbies <= 0){
					this.board.dead = true;
					this.board.threadInfoProtector.release();
					return;
				}

				                                  
                                              
            
     
				
				/* 
				If we have come so far, the game is afoot.

				totalThreads is accurate. 
				Correct playingThreads
				reset quitThreads


				Release permits for threads to play, and the permit to modify thread info
				*/

				this.board.playingThreads = this.board.totalThreads;
				this.board.quitThreads = 0;
				this.board.reentry.release(this.board.playingThreads);
				this.board.registration.release(newbies);
				this.board.threadInfoProtector.release();
    
                                             
                                                          
                                             
			}
			catch (Exception ex){
				System.err.println("An InterruptedException was caught: " + ex.getMessage());
				ex.printStackTrace();
				return;
			}
		}
	}
}
