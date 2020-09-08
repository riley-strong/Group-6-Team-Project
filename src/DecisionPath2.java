/*
    Purpose: This is a sample decision path to follow for CS 3250, Fall 2020, MSU-D - Spring #1
    Author: Dakota Miller
    Date Created: September 6, 2020
    Date Modified: September 7, 2020

    Details: N/A
 */

import java.util.InputMismatchException;
import java.util.Scanner;

public class DecisionPath2 {
    public static void main(String[] args) {
        //imports Scanner class for use in gathering user input
        Scanner input = new Scanner(System.in);

        //establish sentinel values for program
        boolean program_sent = true;
        boolean buyer_sent1 = true;
        boolean buyer_sent2 = true;
        boolean seller_sent = true;
        boolean buy_again = true;

        //initializes & assigns variables
        String welcome_msg = "Welcome to the CompanyName Inventory Manager!";
        welcome_msg += "\nTo exit the program at any time, simply press <Enter>!\n";

        String buy_or_sell_q = "\nWill you be buying (1) or selling (2) inventory today?";

        String user_entry_indicator = ">>> ";

        String buy_or_sell_ans;

        String initial_input_error = "I'm sorry, but that was bad input!";
        initial_input_error += "\nPlease type a 1 if you are buying, 2 if you are selling, or <Enter> to exit.";

        String buyer_product_q = "Please enter the Product ID you are purchasing.\n";
        String buyer_product_ans;

        String buyer_prod_input_error = "I'm sorry, but that was bad input!";
        buyer_prod_input_error += "\nPlease type a 12 digit alphanumeric product ID or <Enter> to exit.";

        String buyer_quantity_q = "How many of this product would you like to purchase?\n";
        int buyer_quantity;
        String buyer_quantity_input_error = "Bad input!\nThat wasn't a whole number greater than 0.";

        String buy_again_q = "Would you like to buy more items? 1 for yes, 2 for no.";
        int buy_again_ans;
        String buy_again_input_error = "Bad input!\n Type 1 to continue buying or type 2 to not continue buying.";

        //custom message to introduce user to program
        System.out.println(welcome_msg + buy_or_sell_q);

        //begin overarching decision-making while loop to make exit cases easier
        while (program_sent) {
            //accept user input
            System.out.print(user_entry_indicator);
            buy_or_sell_ans = input.nextLine();

            //user wishes to exit program
            if (buy_or_sell_ans.equals("")) {
                program_sent = false;
            }

            //user provided input that exceeded desired length
            else if (buy_or_sell_ans.length() != 1) {
                System.out.println(initial_input_error);
            } else if (buy_or_sell_ans.length() == 1) {
                //desired type (int) validation
                if (Character.isDigit(buy_or_sell_ans.charAt(0)) == false) {
                    System.out.println(initial_input_error);
                }

                //buyer is using program to add to inventory
                else if (buy_or_sell_ans.charAt(0) == '1') {
                    System.out.println(buyer_product_q);

                    //continue buying functions until user is complete
                    while (buyer_sent1) {
                        //gather user input
                        System.out.print(user_entry_indicator);
                        buyer_product_ans = input.nextLine();

                        //user wishes to exit program
                        if (buyer_product_ans.equals("")) {
                            buyer_sent1 = false;
                        }

                        //user provided input that exceeded desired length
                        else if (buyer_product_ans.length() != 12) {
                            System.out.println(buyer_prod_input_error);
                        }

                        //proper length input; time for type validations (alphanumeric desired)
                        else if (buyer_product_ans.length() == 12) {
                            //validates character by character that only alphanumerics are used
                            for (int i = 0; i < 12; i++) {
                                if (Character.isLetterOrDigit(buyer_product_ans.charAt(i))) {
                                    buyer_sent1 = true; //essentially a do nothing
                                } else {
                                    System.out.println(buyer_prod_input_error);
                                    break;
                                }
                            }
                            //gather quantity to purchase

                            while (buyer_sent2) {
                                //try-catch block catches any input that's not an integer
                                try {
                                    System.out.println(buyer_quantity_q);
                                    System.out.print(user_entry_indicator);
                                    buyer_quantity = input.nextInt();

                                    if (buyer_quantity <= 0) {
                                        System.out.println(buyer_quantity_input_error);
                                        input.nextLine(); //discards bad input
                                    } else {

                                        // Buyer update methods here


                                        //toggle sentinel value to exit while loop - successful purchase completed
                                        buyer_sent2 = false;

                                        //advises user of purchase details
                                        System.out.println("You successfully purchased " + buyer_quantity +
                                                " of Product ID: " + buyer_product_ans + ".");
                                        System.out.println("Returning you to the main menu!");
                                    }

                                } catch (InputMismatchException ex) {
                                    System.out.println(buyer_quantity_input_error);
                                    input.nextLine(); //discards bad input
                                }
                            }
                            buyer_sent1 = false;
                        }
                    }
                }
                //seller is using program to sell inventory
                else if (buy_or_sell_ans.charAt(0) == '2') {
                    //continue selling functions until user is complete
                    while (seller_sent) {
                        //NEEDS WORK - SHELL CODE INCLUDED CURRENTLY
                        System.out.println("You want to sell inventory!");
                        seller_sent = false;
                    }
                    program_sent = false;
                }
                //single digit entry that wasn't a 1 or 2; error printed & will gather new input
                else //NEEDS WORK
                    System.out.println(initial_input_error);
                //input.nextLine(); //discards bad input
            }
        }
        String exit_message = "Thank you for your interest/business today! We hope to hear from you again soon!";
        System.out.println(exit_message);
    }
}
