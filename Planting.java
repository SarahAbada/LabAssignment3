// Name: Sarah Abada
// Student id: 300201425
//
// The Planting Synchronization Problem
//
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Planting 
{
	static final int MAX  = 3;   // max holes student can be ahead of TA
    static final int REPS = 10;  // how many seeds to plant total

    // --- Semaphores ---
    // initialize each one with the right starting permit count
    static Semaphore emptyHoles  = new Semaphore(0);   // holes ready for a seed
    static Semaphore seededHoles = new Semaphore(0);   // holes ready to be filled
    static Semaphore maxHoles    = new Semaphore(MAX); // throttle on the student
    static Semaphore shovel      = new Semaphore(1);   // mutex: one shovel
	public static void main(String args[]) 
	{
		int i;
	 	// Create Student, TA, Professor threads
		TA ta = new TA();
		Professor prof = new Professor(ta);
		Student stdnt = new Student(ta);

		// Start the threads
		prof.start();
		ta.start();
		stdnt.start();

		// Wait for prof to call it quits
		try {prof.join();} catch(InterruptedException e) { }; 
		// Terminate the TA and Student Threads
		ta.interrupt();
		stdnt.interrupt();
	}   
}

class Student extends Thread
{
	TA ta;

	public Student(TA taThread)
        {
	    ta = taThread;
	}

	public void run()
	{
		while(true)
		{
		     System.out.println("Student: Must wait for TA "+ta.getMAX()+" holes ahead");
			try {Planting.maxHoles.acquire();} catch (Exception e) { break;} // Wait for TA to get ahead
		     System.out.println("Student: Got the go ahead from TA");
			 try {Planting.shovel.acquire();} catch (Exception e) { break;}  // Get the shovel
		     System.out.println("Student: Got the shovel");
		     try {sleep((int) (100*Math.random()));} catch (Exception e) { break;} // Time to dig hole
			 Planting.shovel.release(); // Let go of the shovel
		     Planting.emptyHoles.release();  // hole is ready for seed - increment the number
		     System.out.println("Student: Hole "+ta.getHoleDug()+" is ready for seed");

		     System.out.println("Student: Hole "+ta.getHoleDug()+" is ready to be filled");
		    	
			
		     // Can dig a hole - lets get the shovel
		     //System.out.println("Student: Got the shovel");
	             //try {sleep((int) (100*Math.random()));} catch (Exception e) { break;} // Time to fill hole
                     //ta.incrHoleDug();  // hole filled - increment the number	
		     //System.out.println("Student: Hole "+ta.getHoleDug()+" Dug");
		     //System.out.println("Student: Letting go of the shovel");
		     
		     if(isInterrupted()) break;
		}
		System.out.println("Student is done");
	}
}

class TA extends Thread
{
	// Some variables to count number of holes dug and filled - the TA keeps track of things
	private int holeFilledNum=0;  // number of the hole filled
	private int holePlantedNum=0;  // number of the hole planted
	private int holeDugNum=0;     // number of hole dug
	private final int MAX=5;   // can only get 5 holes ahead
	// add semaphores - the professor lets the TA manage things.
	

	public int getMAX() { return(MAX); }
	public void incrHoleDug() { holeDugNum++; }
	public int getHoleDug() { return(holeDugNum); }
	public void incrHolePlanted() { holePlantedNum++; }
	public int getHolePlanted() { return(holePlantedNum); }

	public TA()
	{
		// Initialise things here
	}
	
	public void run()
	{
		while(true)
		{
			try { Planting.seededHoles.acquire(); } catch (Exception e) { break; }  // Wait for a hole to be ready for filling
			try { Planting.shovel.acquire(); } catch (Exception e) { break; }  // Get the shovel
		     System.out.println("TA: Got the shovel");
	             try {sleep((int) (100*Math.random()));} catch (Exception e) { break;} // Time to fill hole
                     holeFilledNum++;  // hole filled - increment the number
			 Planting.shovel.release();  // Let go of the shovel	
		    		     System.out.println("TA: Letting go of the shovel");  
			Planting.maxHoles.release();  // let TA know that there is one more hole ready for filling	
					 System.out.println("TA: The hole "+holeFilledNum+" has been filled");

		     
		     if(isInterrupted()) break;
		}
		System.out.println("TA is done");
	}
}

class Professor extends Thread
{
	TA ta;

	public Professor(TA taThread)
        {
	    ta = taThread;
	}

	public void run()
	{
		for (int i = 0; i < Planting.REPS; i++)
		{
			try { Planting.emptyHoles.acquire(); } catch (Exception e) { break; }  // Wait for a hole to be ready for planting
	        try {sleep((int) (50*Math.random()));} catch (Exception e) { break;} // Time to plant seed
		     Planting.seededHoles.release();  // hole is ready to be filled - increment the number
                     ta.incrHolePlanted();  // the seed is planted - increment the number	
		     System.out.println("Professor: All be advised that I have completed planting hole "+
				        ta.getHolePlanted());
		}
		System.out.println("Professeur: We have worked enough for today");
	}
}
