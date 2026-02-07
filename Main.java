import java.util.*;

class CinemaHall{
  int[][] hall;
  int[] availability;
  int[] cost;
  int[] showtimes;

  CinemaHall(int rows, int cols, int[] cost, int[] showtimes){
    this.hall=new int[rows][cols];
    this.availability=new int[rows];
    this.cost=cost;
    this.showtimes=showtimes;

    for (int i=0;i<rows;i++){
      availability[i]=hall[0].length;
    }
  }

  public void checkAvl(){
    System.out.println("The Hall grid is based on: 0->Non-booked and 1->Booked seats.");
    for (int i=0;i<hall.length;i++){
      System.out.printf("Row %d:",i);
      for (int j=0;j<hall[0].length;j++){
        System.out.printf("%d ",hall[i][j]);
      }
      System.out.println();
    }
  }
}

abstract class Booking{
  CinemaHall ch;

  Booking(CinemaHall ch){
    this.ch=ch;
  }

  public boolean vipCheck(int[][] arr){
    // assuming user seats in the same row
    int row=arr[0][0];
    int firstCol=arr[0][1];
    int lastCol=arr[arr.length-1][1];

    if (row > 0) {
      for (int col = firstCol; col <= lastCol; col++) {
        if (ch.hall[row - 1][col] != 0) return false;
      }
    }

    if (row < ch.hall.length - 1) {
      for (int col = firstCol; col <= lastCol; col++) {
        if (ch.hall[row + 1][col] != 0) return false;
      }
    }

    if (firstCol!=0 && ch.hall[row][firstCol-1]!=0){
      return false;
    }
    if (lastCol!=ch.hall[0].length-1 && ch.hall[row][lastCol+1]!=0){
      return false;
    }

    return true;
  }

  public boolean isolatedCheck(int[][] arr){
    // assuming user seats in the same row
    for (int i=0;i<arr.length-1;i++){
      if (Math.abs(arr[i+1][1]-arr[i][1])!=1){
        return false;
      }
    }
    return true;
  }

  abstract boolean bookingMethod(int count,HashMap<String,int[][]> customerHashMap);
  abstract void cancelBooking(HashMap<String,int[][]> customerHashMap);
}

class Manual extends Booking{

  int[][] seatsBooked;
  int totSeats;
  String vip;
  int bookingTotal;
  Scanner sc=new Scanner(System.in);

  Manual(CinemaHall ch,String vip){
    super(ch);
    this.vip=vip;
  }

  public boolean validateSeats(){
    for (int[] seat:seatsBooked){
      if (seat[0]<0 ||seat[0]>=ch.hall.length){
        return false;
      }
      if (seat[1]<0 || seat[1]>=ch.hall[0].length){
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean bookingMethod(int count,HashMap<String,int[][]> customerHashMap){
    System.out.println("Check the availability of seats and book yours now!");
    ch.checkAvl();
    System.out.print("Enter the no. of seats you want to book:");
    totSeats=sc.nextInt();
    sc.nextLine();

    seatsBooked=new int[totSeats][2];

    int sum = Arrays.stream(ch.availability).sum();
    if (totSeats>sum){
      System.out.println("Not enough seats.");
      return false;
    }

    for (int i=0;i<totSeats;i++){
      System.out.println("Enter your seats with row and column number:");
      seatsBooked[i][0]=sc.nextInt();
      seatsBooked[i][1]=sc.nextInt();
    }

    boolean valid=validateSeats();
    if (!valid){
      System.out.println("The seats chosen does not exist.");
      return false;
    }

    boolean isolatedValid=isolatedCheck(seatsBooked);
    if (!isolatedValid){
      System.out.println("The currently chosen seats are against our policy, choose seats without creating gaps.");
      return false;
    }

    if (vip.toLowerCase().equals("yes")){
      boolean vipValid=vipCheck(seatsBooked);
      if (!vipValid){
        System.out.println("The seats chosen aren't private enough, book another seat.");
        return false;
      }
    }

    for(int[] seat: seatsBooked){
      if (ch.hall[seat[0]][seat[1]]==0){
        ch.hall[seat[0]][seat[1]]=1;
        bookingTotal+=ch.cost[seat[0]];
      }
      else{
        System.out.println("The seat chosen is already booked.");
        return false;
      }
    }
    String CID="C"+count;
    System.out.printf("Booking successful. The booking total is: %d\n",bookingTotal);
    System.out.printf("Your customer ID is: %s\n",CID);
    customerHashMap.put(CID,seatsBooked);
    return true;
  }

  public void cancelBooking(HashMap<String,int[][]> customerHashMap){
    System.out.println("Enter the customer ID to cancel booking:");
    String cusId=sc.nextLine();
    int found=0;

    for (Map.Entry<String, int[][]> cus : customerHashMap.entrySet()){
      if (cus.getKey().equals(cusId)){
        found=1;
        System.out.println("Enter the no. of seats to be cancelled:");
        int noOfCancelSeats=sc.nextInt();

        int[][] cancelSeats=new int[noOfCancelSeats][2];
        for (int i=0;i<noOfCancelSeats;i++){
          System.out.println("Enter your seats with row and column number:");
          cancelSeats[i][0]=sc.nextInt();
          cancelSeats[i][1]=sc.nextInt();
        }
        int seatFound=0;
        for (int[] cancelSeat : cancelSeats ) {
          for (int[] seat : cus.getValue()) {
            if (java.util.Arrays.equals(seat, cancelSeat)) {
              seatFound+=1;
              break;
            }
            else{
              seatFound=0;
            }
          }
        }
        if (seatFound==noOfCancelSeats){
          for(int[] seat: cancelSeats){
            ch.hall[seat[0]][seat[1]]=0;
            int[][] booked=customerHashMap.get(cusId);
            for (int i = 0; i < booked.length; i++) {
              if (booked[i][0] == seat[0] && booked[i][1] == seat[1]) {
                  booked[i][0] = -1; 
                  booked[i][1] = -1; 
              }
            }
            customerHashMap.put(cusId,booked);
          }
          System.out.println("Booking cancelled successfully.");
        }
        else{
          System.out.println("Seat not found, invalid seat given.");
          return;
        }
      }
    }
    if(found==0){
      System.out.println("Customer ID not found.");
      return;
    }
  }
}

public class Main {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
  
    int[] costs = {100, 150, 200, 250, 300};
    int[] shows = {12, 15, 18};
    CinemaHall hall = new CinemaHall(5, 5, costs, shows);
    
    HashMap<String, int[][]> customerData = new HashMap<>();
    
    int bookingCount=0;
    
    boolean running = true;
    while (running) {
      System.out.println("Do you want to enter VIP mode (yes/no):");
      String vip=sc.nextLine();
      Manual bookingSystem = new Manual(hall, vip);
      System.out.println("\nCinema Booking System:\n");
      System.out.println("1. Check Seat Availability");
      System.out.println("2. Book Seats (Manual)");
      System.out.println("3. Cancel Booking");
      System.out.println("3. Exit system.");
      System.out.print("Enter your choice: ");
      
      int choice = sc.nextInt();
      sc.nextLine();

      switch (choice) {
        case 1:
          hall.checkAvl();
          break;
        case 2:
          boolean success=bookingSystem.bookingMethod(bookingCount, customerData);
          if (success){
            bookingCount+=1;
          }
          break;
        case 3:
          bookingSystem.cancelBooking(customerData);
          break;
        case 4:
          System.out.println("Exiting system.");
          running = false;
          break;
        default:
          System.out.println("Invalid choice.");
      }
    }
    sc.close();
  }
}
