import java.sql.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.time.*;

public class CSCI3170Proj{


    public static void main(String[] args) {

        String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db54";
        String dbUsername = "Group54";
        String dbPassword = "CSCI3170";

        Connection con = null;
        try {
            // Connect
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword) ;
            Statement stmt = con.createStatement();

            // Start
            app(con, stmt);
            con.close();
        }
        catch(ClassNotFoundException e){
            System.out.println("DB Driver Not found");
            System.exit(0);
        }
        catch(SQLException e){
             System.out.println(e);
        }

    }

    public static void app(Connection con, Statement stmt) {
        //System.out.println("-----Main menu-----");
        //System.out.println("What kinds of operation would you like to perform?");
        Scanner sc = new Scanner(System.in);

        boolean flag = true;
        while (flag) {
            String input;
            while (true) {
                System.out.println("-----Main menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. Operations for administrator");
                System.out.println("2. Operations for library user");
                System.out.println("3. Operations for librarian");
                System.out.println("4. Exit this program");
                System.out.print("Enter Your Choice: ");

                input = sc.nextLine();
                if (checkInput(input, 1, 4)) {
                    //flag = false;
                    break;
                }
            }
            int int_input = Integer.parseInt(input);
            if (int_input == 4)
                break;

            switch (int_input) {
                case 1:
                    System.out.print("\n");
                    Admin(con, stmt);
                    break;
                case 2:
                    System.out.print("\n");
                    libUser(con, stmt);
                    break;
                case 3:
                    System.out.print("\n");
                    librarian(con, stmt);
                    break;
                case 4:
                    flag = false;
                    break;
            }
        }
    }

    public static void Admin(Connection con, Statement stmt) {
        Scanner scan = new Scanner(System.in);
        boolean flag = true;
        while (flag) {
            String input;
            int operation_number;
            while (true) {
                System.out.println("-----Operations for administrator menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. Create all tables");
                System.out.println("2. Delete all tables");
                System.out.println("3. Load from datafile");
                System.out.println("4. Show number of records in each table");
                System.out.println("5. Return to the main menu");
                System.out.print("Enter Your Choice: ");

                input = scan.nextLine();
                if (checkInput(input, 1, 5)) {
                    //flag = false;
                    break;
                }
            }
            int int_input = Integer.parseInt(input);

            switch (int_input) {
                case 1:
                    createAllTable(con, stmt);
                    System.out.print("\n");
                    break;
                    //Admin(con, stmt);
                case 2:
                    deleteAllTable(stmt);
                    System.out.print("\n");
                    break;
                    //Admin(con, stmt);
                case 3:
                    loadData(con, stmt);
                    System.out.print("\n");
                    break;
                    //Admin(con, stmt);
                case 4:
                    showRecordCount(stmt);
                    System.out.print("\n");
                    break;
                    //Admin(con, stmt);
                case 5:
                    flag = false;
                    System.out.print("\n");
                    return;
            }
        }

    }

    // For the Admin
    public static void createAllTable(Connection con, Statement stmt){
        deleteAllTable(stmt);
        System.out.println("[Processing] Creating the database table");
        try {

            stmt.executeUpdate("CREATE TABLE user_category ( ucid INT NOT NULL , max INT NOT NULL, period INT NOT NULL, PRIMARY KEY(ucid))");
            stmt.executeUpdate("CREATE TABLE libuser(libuid CHAR(10) NOT NULL, name VARCHAR(25) NOT NULL, age INT NOT NULL, address VARCHAR(100) NOT NULL,ucid INT NOT NULL,"+
                                 "PRIMARY KEY (libuid),FOREIGN KEY (ucid) REFERENCES user_category (ucid) ON DELETE NO ACTION)");

            stmt.executeUpdate("CREATE TABLE book_category(bcid INT NOT NULL, bcname VARCHAR(30) NOT NULL, PRIMARY KEY(bcid))");
            stmt.executeUpdate("CREATE TABLE book(callnum CHAR(8) NOT NULL, title VARCHAR(30) NOT NULL, publish DATE NOT NULL, rating FLOAT NULL,tborrowed INT NOT NULL,bcid INT NOT NULL," +
                                "PRIMARY KEY (callnum),FOREIGN KEY (bcid) REFERENCES book_category (bcid) ON DELETE NO ACTION)");

            stmt.executeUpdate("CREATE TABLE copy(callnum CHAR(8) NOT NULL, copynum INT NOT NULL, PRIMARY KEY (callnum,copynum), FOREIGN KEY (callnum) REFERENCES book (callnum) ON DELETE NO ACTION ON UPDATE NO ACTION)");
            stmt.executeUpdate("CREATE TABLE borrow(libuid CHAR(10) NOT NULL, callnum CHAR(8) NOT NULL, copynum INT NOT NULL, checkout DATE NOT NULL, `return` DATE NULL, "+
                                "PRIMARY KEY(libuid,callnum,copynum,checkout), FOREIGN KEY (libuid) REFERENCES libuser(libuid) ON DELETE NO ACTION , " +
                                "FOREIGN KEY (callnum) REFERENCES book(callnum) ON DELETE NO ACTION)");
            stmt.executeUpdate("CREATE TABLE authorship(aname VARCHAR(25) NOT NULL, callnum CHAR(8) NOT NULL, " +
                                "PRIMARY KEY(aname,callnum), FOREIGN KEY (callnum) REFERENCES book(callnum) ON DELETE NO ACTION)");
            System.out.println("[SUCESS] Create All Table");
            System.out.println();
        } catch (Exception e) {
            System.out.println("[Error] Cannot Create All Table");
            System.out.println(e);
        }
    }

    public static void deleteAllTable(Statement stmt){
        System.out.println("[Processing] Deleteing All table");
        try {
            stmt.executeUpdate("DROP TABLE IF EXISTS authorship");
            stmt.executeUpdate("DROP TABLE IF EXISTS borrow");
            stmt.executeUpdate("DROP TABLE IF EXISTS copy");
            stmt.executeUpdate("DROP TABLE IF EXISTS book");
            stmt.executeUpdate("DROP TABLE IF EXISTS book_category");
            stmt.executeUpdate("DROP TABLE IF EXISTS libuser");
            stmt.executeUpdate("DROP TABLE IF EXISTS user_category");

            System.out.println("[SUCESS] Delete All Table");
            System.out.println();
        } catch (Exception e) {
            System.out.println("[Error] Cannot Delete All Table");
            System.out.println(e);
        }
    }

    public static void loadData(Connection con, Statement stmt){
        String folder_name = "/sample_data";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        // load user_category
        try {
            File user_category = new File(System.getProperty("user.dir") + folder_name + "/user_category.txt");
            Scanner read_data = new Scanner(user_category);
            while(read_data.hasNextLine()){
                String[] line = read_data.nextLine().split("\t");
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO user_category VALUES(?,?,?)");
                    ps.setInt(1,Integer.parseInt(line[0]));
                    ps.setInt(2,Integer.parseInt(line[1]));
                    ps.setInt(3,Integer.parseInt(line[2]));
                    ps.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            read_data.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //load book_category
        try {
            File book_category = new File(System.getProperty("user.dir") + folder_name + "/book_category.txt");
            Scanner read_data = new Scanner(book_category);
            while(read_data.hasNextLine()){
                String[] line = read_data.nextLine().split("\t");
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO book_category VALUES(?,?)");
                    ps.setInt(1,Integer.parseInt(line[0]));
                    ps.setString(2,line[1]);
                    ps.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            read_data.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


        //load user
        try {
            File user = new File(System.getProperty("user.dir") + folder_name + "/user.txt");
            Scanner read_data = new Scanner(user);
            while(read_data.hasNextLine()){
                String[] line = read_data.nextLine().split("\t");
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO libuser VALUES(?,?,?,?,?)");
                    ps.setString(1,line[0]); //libuid
                    ps.setString(2,line[1]); //name
                    ps.setInt(3,Integer.parseInt(line[2])); //age
                    ps.setString(4,line[3]); // address;
                    ps.setInt(5,Integer.parseInt(line[4])); //ucid
                    ps.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            read_data.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //load book
        try {
            File book = new File(System.getProperty("user.dir") + folder_name + "/book.txt");
            Scanner read_data = new Scanner(book);
            PreparedStatement ps;
            while(read_data.hasNextLine()){
                String[] line = read_data.nextLine().split("\t");
                try {
                    ps = con.prepareStatement("INSERT INTO book VALUES(?,?,?,?,?,?)");
                    ps.setString(1,line[0]); //callnum
                    ps.setString(2,line[2]); //title

                    java.util.Date date = sdf.parse(line[4]);
                    long lg = date.getTime();
                    ps.setDate(3,new java.sql.Date(lg)); //publish date
                    if(!line[5].equals("null")){
                        ps.setFloat(4,Float.parseFloat(line[5])); //rating
                    }else{
                        ps.setNull(4,java.sql.Types.NULL);
                    }

                    ps.setInt(5,Integer.parseInt(line[6]));//time borrowed;
                    ps.setInt(6,Integer.parseInt(line[7]));//bcid;
                    ps.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
                try {
                    ps = con.prepareStatement("INSERT INTO copy VALUES(?,?)");
                    int i = 0;
                    while(i<Integer.parseInt(line[1])){
                        ps.setString(1,line[0]); //callnum
                        ps.setInt(2,i+1); //copy number
                        ps.execute();
                        i++;
                    }
                    i=0;
                    ps = con.prepareStatement("INSERT INTO authorship VALUES(?,?)");
                    while(i<line[3].split(",").length){
                        ps.setString(1,line[3].split(",")[i]);
                        ps.setString(2,line[0]); //call num
                        ps.execute();
                        i++;
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }

            }
            read_data.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //load checkout to borrow table NOT FINISH
        try {
            PreparedStatement ps;
            File check_out = new File(System.getProperty("user.dir") + folder_name + "/check_out.txt");
            Scanner read_data = new Scanner(check_out);
            while(read_data.hasNextLine()){
                String[] line = read_data.nextLine().split("\t");
                try {
                    ps = con.prepareStatement("INSERT INTO borrow VALUES(?,?,?,?,?)");

                    ps.setString(1,line[2]); //lib user id
                    ps.setString(2,line[0]); //callnum
                    ps.setInt(3,Integer.parseInt(line[1]));
                    java.util.Date date = sdf.parse(line[3]);
                    long lg = date.getTime();
                    ps.setDate(4,new java.sql.Date(lg));//checkout date

                    if(line[4].equals("null")){
                        ps.setNull(5,java.sql.Types.NULL);
                    }else{
                        date = sdf.parse(line[4]);
                        lg = date.getTime();
                        ps.setDate(5,new java.sql.Date(lg));
                    }
                    ps.execute();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            read_data.close();
            System.out.println("[Sucess]: Load the data into the database");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showRecordCount(Statement stmt){
        try {
            System.out.println("Number of records in each table:");

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM user_category");
            rs.next();
            System.out.println("user_category: "+ rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM libuser");
            rs.next();
            System.out.println("libuser: "+ rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM book_category");
            rs.next();
            System.out.println("book_category: "+ rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM book");
            rs.next();
            System.out.println("book: "+ rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM copy");
            rs.next();
            System.out.println("copy: "+ rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow");
            rs.next();
            System.out.println("borrow: "+ rs.getString(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM authorship");
            rs.next();
            System.out.println("authorship: "+ rs.getString(1));

        } catch (Exception e) {
            System.out.println(e);
        }
    }







    public static void libUser(Connection con, Statement stmt) {
        Scanner scan = new Scanner(System.in);
        boolean flag = true;
        while (flag) {
            String input;
            int operation_number;
            while (true) {
                System.out.println("-----Operations for library user menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. Search for Books");
                System.out.println("2. Show load record of a user");
                System.out.println("3. Return to the main menu");
                System.out.print("Enter Your Choice: ");
                input = scan.nextLine();
                if (checkInput(input, 1, 3)) {
                    //flag = false;
                    break;
                }
            }
            int int_input = Integer.parseInt(input);

            switch (int_input) {
                case 1:
                    searchBook(con, stmt);
                    System.out.print("\n");
                    break;
                case 2:
                    loanRec(con, stmt);
                    System.out.print("\n");
                    break;
                case 3:
                    System.out.println();
                    flag = false;
                    //app(con, stmt);
            }
        }
    }



    public static void loanRec(Connection con, Statement stmt){
    
        Scanner scan = new Scanner(System.in);
        String input = "000";
        boolean flag = true;
        String libuid = "000";
        while (flag) {

            System.out.println("Enter The User ID: ");
            input = scan.nextLine();
            if (input.length() == 10) {
                    flag = false;
                    //break;
            }
            else{
                System.out.println("The User ID should be string with 10 Character");
       
			}
        }


        try{
            libuid = input;
            PreparedStatement pre_st = null;
            String query = "SELECT * FROM borrow WHERE libuid = ?";

            pre_st = con.prepareStatement(query);
            pre_st.setString(1, libuid);

            System.out.println("Loan Record");
            System.out.println("|CallNum|CopyNum|Title|Author|Check-out|Returned?|");
            ResultSet libuid_result = pre_st.executeQuery();
            
            for (boolean flag2 = false; libuid_result.next(); flag2 = true) {
                System.out.print('|'+libuid_result.getString("callnum")+'|'+libuid_result.getString("copynum"));
                String callnum_uid = libuid_result.getString("callnum");

                //Get Title
                pre_st = con.prepareStatement("SELECT title FROM book WHERE BINARY callnum = ?");
                pre_st.setString(1, callnum_uid);
                ResultSet book_result = pre_st.executeQuery();

                book_result.next();
                System.out.print('|');
                System.out.print(book_result.getString("title"));
                System.out.print('|');


                //Get author
                pre_st = con.prepareStatement("SELECT aname FROM authorship WHERE BINARY callnum = ?");
                pre_st.setString(1, callnum_uid);
                ResultSet author_result = pre_st.executeQuery();

                System.out.print('|');
                for (boolean flag3 = false; author_result.next(); flag3 = true) {
                            if (flag3)
                                System.out.print(", ");
                            System.out.print(author_result.getString("aname"));
                }


                System.out.print('|');
                System.out.print(libuid_result.getString("checkout"));


                System.out.print('|');
                if(libuid_result.getString("return") == null)
                    System.out.println("No|");
                else
                    System.out.println("Yes|");

            }

            
        }
        catch (SQLException e) {
            System.out.println("[Error]: " + "No User Record Found");
        }
    
    
    
    }


    public static void searchBook(Connection con, Statement stmt){

        Scanner scan = new Scanner(System.in);
        String input = "00";
        boolean flag = true;
        int int_input = 0;
        while (flag) {

            System.out.println("Choose the Search criterion");
            System.out.println("1. call number");
            System.out.println("2. title");
            System.out.println("3. author");
            while(true){
                System.out.println("Choose the Search criterion: ");
                input = scan.nextLine();
                if (checkInput(input, 1, 3)) {
                        flag = false;
                        break;
                    } 
            }
            
        }
        int_input = Integer.parseInt(input);
            switch (int_input) {
                case 1:
                    callnum(con, stmt);
                    System.out.print("\n");
                    System.out.print("End of Query\n");
                    break;
                case 2:
                    title(con, stmt);
                    System.out.print("\n");
                    System.out.print("End of Query\n");
                    break;
                case 3:
                    author(con, stmt);
                    System.out.print("\n");
                    System.out.print("End of Query\n");
                    break;
            }



    }




    public static boolean check_digtchar(String input){

    int len = input.length();
        for (int i = 0; i < len; i++) {
            if ((Character.isLetterOrDigit(input.charAt(i)) == false)) {
                return false;

            }
        }
        return true;

    }


    public static void callnum(Connection con, Statement stmt){


        Scanner scan = new Scanner(System.in);
        String input = "A1";
        boolean flag = true;

        //while (flag) {
            while (true) {
                System.out.println("Type in the Search Keyword: ");
                input = scan.nextLine();

                if (input.length() != 8 ) {
                    System.out.print("The length of call number should equal to 8\n");
                }
                else if(check_digtchar(input) == false){

                    System.out.print("The call number should only contain number or digit");

                }
                else{
                    flag = false;
                    break;
                }
                }
        //}
        try{
            PreparedStatement pre_st = null;
            String query = "SELECT * FROM book WHERE BINARY callnum = ?";

            pre_st = con.prepareStatement(query);
            pre_st.setString(1, input);

            System.out.println("|Call Num|Title|Book Category|Author|Available No. of Copy|");
            ResultSet result = pre_st.executeQuery();
            result.next();

            String callnum_au = result.getString("callnum");
            System.out.print('|'+ result.getString("callnum"));

            System.out.print('|'+ result.getString("title"));

            //Get Category
            String callnum_bcid = result.getString("bcid");
            pre_st = con.prepareStatement("SELECT bcname FROM book_category WHERE BINARY bcid = ?");
            pre_st.setString(1, callnum_bcid);
            ResultSet cat_result = pre_st.executeQuery();

            cat_result.next();
            System.out.print('|');
            System.out.print(cat_result.getString("bcname"));


            //Get author
            pre_st = con.prepareStatement("SELECT aname FROM authorship WHERE BINARY callnum = ?");
            pre_st.setString(1, callnum_au);
            ResultSet author_result = pre_st.executeQuery();

            System.out.print('|');
            for (boolean flag2 = false; author_result.next(); flag2 = true) {
                        if (flag2)
                            System.out.print(", ");
                        System.out.print(author_result.getString("aname"));
            }

            System.out.print('|');
            System.out.print(result.getString("rating"));

            // Copies in lib
            pre_st = con.prepareStatement("SELECT COUNT(*) FROM copy WHERE BINARY callnum = ?");
            pre_st.setString(1, callnum_au);
            ResultSet copy_result = pre_st.executeQuery();
            copy_result.next();

            int copy_inlib = copy_result.getInt(1);
            System.out.print(copy_inlib);

            // Copies borrowed 
            pre_st = con.prepareStatement("SELECT COUNT(*) FROM borrow WHERE BINARY callnum = ? AND `return` IS NULL");
            pre_st.setString(1, callnum_au);
            ResultSet copy_result_bor = pre_st.executeQuery();
            copy_result_bor.next();
            int copy_bor = copy_result_bor.getInt(1);

            System.out.print('|');
            System.out.print(copy_inlib-copy_bor);

            System.out.print('|');


        }
        catch (SQLException e) {
            System.out.println("[Error]: " + "No matched call number");
        }


    }



    public static void librarian(Connection con, Statement stmt) {
        Scanner scan = new Scanner(System.in);
        boolean flag = true;
        while (flag) {
            String input;
            int operation_number;
            while (true) {
                System.out.println("-----Operations for Librarian menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. Book Borrowing");
                System.out.println("2. Book Returning");
                System.out.println("3. List all un-returned book copies which are checked-out within a period");
                System.out.println("4. Return to the main menu");
                System.out.print("Enter Your Choice: ");

                input = scan.nextLine();
                if (checkInput(input, 1, 4)) {
                    //flag = false;
                    break;
                }
            }
            int int_input = Integer.parseInt(input);

            switch (int_input) {
                case 1:
                    borrowBookCopy(con,stmt);
                    System.out.print("\n");

                    break;
                case 2:
                    returnBookCopy(con,stmt);
                    System.out.print("\n");
                    break;
                case 3:
                    listAllUnReturnedBook(con,stmt);
                    System.out.print("\n");
                    //operation
                    break;
                case 4:
                    System.out.print("\n");
                    flag = false;
            }
        }
    }

    public static void borrowBookCopy(Connection con, Statement stmt){

        Scanner scan = new Scanner(System.in);
        String user_id;
        int user_max_book;
        PreparedStatement ps;
        ResultSet rs;
        boolean loop = true;
        while(true){
            System.out.print("Enter The User ID: ");
            user_id = scan.nextLine();
            if(checkInputLength(user_id,10,10)){
                break;
            }
            
        }
        

        String call_number = "";
        while(true){
            System.out.print("Enter The Call Number: ");
            call_number = scan.nextLine();
            if(checkInputLength(call_number,8,8)){
               break;
            }
        }
        
        String copy_number;
        while(true){
            System.out.print("Enter The Copy Number: ");
            copy_number = scan.nextLine();
            if(checkInputLength(copy_number,1,1)){
                break;
            }
                
        }

        try {
            ps = con.prepareStatement("SELECT libuid,ucid FROM libuser WHERE libuid = ?");
            ps.setString(1,user_id);
            rs = ps.executeQuery();
            int ucid = 0;
            if(!rs.next()){
                System.out.println("[Error]: The User ID does not exist");
                return;
            }else{
                ucid = rs.getInt(2);
            }

            ps = con.prepareStatement("SELECT callnum FROM book WHERE callnum = ?");
            ps.setString(1,call_number);
            rs = ps.executeQuery();
            if(!rs.next()){
                System.out.println("[Error]: The Call Number does not exist");
                return;
            }

            ps = con.prepareStatement("SELECT COUNT(*) FROM copy WHERE callnum = ? AND copynum = ?");
            ps.setString(1,call_number);
            ps.setInt(2,Integer.parseInt(copy_number));
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) != 1){
                System.out.println("[Error]: The Copy Number does not exist");
                return; 
            }


            ps = con.prepareStatement("SELECT max FROM user_category WHERE ucid = ?");
            ps.setInt(1,ucid);
            rs = ps.executeQuery();
            rs.next();
            user_max_book = rs.getInt(1);

            ps = con.prepareStatement("SELECT COUNT(*) FROM borrow WHERE libuid = ? AND `return` IS NULL");
            ps.setString(1,user_id);
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) >= user_max_book){
                System.out.println("[Error]: The user borrow too many book");
                return;
            }

            ps = con.prepareStatement("SELECT COUNT(*) FROM borrow WHERE callnum = ? AND copynum = ?  AND `return` IS NULL");
            ps.setString(1,call_number);
            ps.setInt(2,Integer.parseInt(copy_number));
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) != 0){
                System.out.println("[Error]: This book has been borrowed");
                return;
            }

            //no error can borrow
            ps = con.prepareStatement("INSERT INTO borrow VALUES (?,?,?,?,?)");
            ps.setString(1,user_id);
            ps.setString(2,call_number);
            ps.setInt(3,Integer.parseInt(copy_number));
            ps.setDate(4,new java.sql.Date(System.currentTimeMillis()));
            ps.setNull(5,java.sql.Types.NULL);
            ps.execute();
            System.out.println("[Success]: Book borrowing performed successfully.");



        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void returnBookCopy(Connection con, Statement stmt){

        Scanner scan = new Scanner(System.in);
        String user_id;
        int user_max_book;
        PreparedStatement ps;
        ResultSet rs;
        while(true){
            System.out.print("Enter The User ID: ");
            user_id = scan.nextLine();
            if(checkInputLength(user_id,10,10)){
                break;
            }
            
        }
        

        String call_number = "";
        while(true){
            System.out.print("Enter The Call Number: ");
            call_number = scan.nextLine();
            if(checkInputLength(call_number,8,8)){
               break;
            }
        }
        
        String copy_number;
        while(true){
            System.out.print("Enter The Copy Number: ");
            copy_number = scan.nextLine();
            if(checkInputLength(copy_number,1,1)){
                break;
            }
                
        }
        String user_rating;
        while(true){
            System.out.print("Enter Your Rating of the Book: ");
            user_rating = scan.nextLine();
            if(checkInputLength(user_rating,1,1)){
                break;
            }
                
        }


        try {
            ps = con.prepareStatement("SELECT COUNT(*) FROM borrow WHERE libuid = ? AND callnum = ? AND copynum = ?  AND `return` IS NULL");
            ps.setString(1,user_id);
            ps.setString(2,call_number);
            ps.setInt(3,Integer.parseInt(copy_number));
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) == 0){
                System.out.println("[Error]: No borrow record for this book");
                return;
            } 

            //update return record
            Float current_rating;
            ps = con.prepareStatement("SELECT rating FROM book WHERE callnum = ?");
            ps.setString(1,call_number);
            rs = ps.executeQuery();
            if(rs.next()){
                current_rating = rs.getFloat(1);
            }else{
                current_rating = 0f;
            }
            // System.out.println("old rating" + current_rating);

            int time_borrowed;
            ps = con.prepareStatement("SELECT tborrowed FROM book WHERE callnum = ?");
            ps.setString(1,call_number);
            rs = ps.executeQuery();
            rs.next();
            time_borrowed = rs.getInt(1);

            Float new_rating = ((current_rating * time_borrowed) + Float.parseFloat(user_rating)) / (time_borrowed + 1);
            ps = con.prepareStatement("UPDATE borrow SET `return` = ? WHERE libuid = ? AND callnum = ? AND copynum = ?");
            ps.setDate(1,new java.sql.Date(System.currentTimeMillis()));
            ps.setString(2,user_id);
            ps.setString(3,call_number);
            ps.setInt(4,Integer.parseInt(copy_number));
            ps.execute();

            ps = con.prepareStatement("UPDATE book SET rating = ? , tborrowed = ? WHERE callnum = ?");
            ps.setFloat(1,new_rating);
            ps.setInt(2,time_borrowed+1);
            ps.setString(3,call_number);
            ps.execute();

            System.out.println("[Success]: book returning performed successfully.");
            // System.out.println("new rating" + new_rating);

        } catch (Exception e) {
            System.out.println(e);
        }
        

    }

    public static void listAllUnReturnedBook(Connection con, Statement stmt){

        PreparedStatement ps;
        ResultSet rs;
        Scanner scan = new Scanner(System.in);
        String starting_date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date s_date;
        java.util.Date e_date;

        while(true){
            System.out.print("Type in the starting date [dd/mm/yyyy]: ");
            starting_date = scan.nextLine();
            if(checkInputDate(starting_date)){
                break;
            }
        }

        String ending_date;
        while(true){
            System.out.print("Type in the ending date [dd/mm/yyyy]: ");
            ending_date = scan.nextLine();
            if(checkInputDate(ending_date)){        
                break;
            }
        }
        try {
            s_date = sdf.parse(starting_date);
            e_date = sdf.parse(ending_date);
            long lg1 = s_date.getTime();
            long lg2 = e_date.getTime();
            if(s_date.compareTo(e_date) <= 0){

            }else{
                System.out.println("[Error]: The ending date is early than staring date");
                return;
            }

            ps = con.prepareStatement("SELECT libuid,callnum,copynum,checkout FROM borrow WHERE checkout BETWEEN ? AND ?");
            ps.setDate(1,new java.sql.Date(lg1));
            ps.setDate(2,new java.sql.Date(lg2));
            rs = ps.executeQuery();
            System.out.println("List of UnReturned Book:");
            System.out.println("|LibUID|CallNum|CopyNum|Checkout|");
            while(rs.next()){
                System.out.println("|" + rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getInt(3) + "|" + rs.getDate(4) + "|");
            }
            System.out.println("End of Query");
        } catch (Exception e) {
            System.out.println(e);
        }
        

    }




    public static void use_callnum_getinfo(String callnum, Connection con, Statement stmt){
    
    String input = callnum;
    try{
            PreparedStatement pre_st = null;
            String query = "SELECT * FROM book WHERE BINARY callnum = ?";

            pre_st = con.prepareStatement(query);
            pre_st.setString(1, input);

            //System.out.println("|Call Num|Title|Book Category|Author|Available No. of Copy|");
            ResultSet result = pre_st.executeQuery();
            result.next();

            String callnum_au = result.getString("callnum");
            System.out.print('|'+ result.getString("callnum"));

            System.out.print('|'+ result.getString("title"));

            //Get Category
            String callnum_bcid = result.getString("bcid");
            pre_st = con.prepareStatement("SELECT bcname FROM book_category WHERE BINARY bcid = ?");
            pre_st.setString(1, callnum_bcid);
            ResultSet cat_result = pre_st.executeQuery();

            cat_result.next();
            System.out.print('|');
            System.out.print(cat_result.getString("bcname"));


            //Get author
            pre_st = con.prepareStatement("SELECT aname FROM authorship WHERE BINARY callnum = ?");
            pre_st.setString(1, callnum_au);
            ResultSet author_result = pre_st.executeQuery();

            System.out.print('|');
            for (boolean flag2 = false; author_result.next(); flag2 = true) {
                        if (flag2)
                            System.out.print(", ");
                        System.out.print(author_result.getString("aname"));
            }

            System.out.print('|');
            System.out.print(result.getString("rating"));

            // Copies in lib
            pre_st = con.prepareStatement("SELECT COUNT(*) FROM copy WHERE BINARY callnum = ?");
            pre_st.setString(1, callnum_au);
            ResultSet copy_result = pre_st.executeQuery();
            copy_result.next();

            int copy_inlib = copy_result.getInt(1);
            System.out.print(copy_inlib);

            // Copies borrowed 
            pre_st = con.prepareStatement("SELECT COUNT(*) FROM borrow WHERE BINARY callnum = ? AND `return` IS NULL");
            pre_st.setString(1, callnum_au);
            ResultSet copy_result_bor = pre_st.executeQuery();
            copy_result_bor.next();
            int copy_bor = copy_result_bor.getInt(1);

            System.out.print('|');
            System.out.print(copy_inlib-copy_bor);

            System.out.print("|\n");


        }
        catch (SQLException e) {
            System.out.println("[Error]: " + "No mathced call number found");
        }
    
    
    
    }

    public static void title(Connection con, Statement stmt){
     
        Scanner scan = new Scanner(System.in);
        String input = "A1";
        boolean flag = true;

        while (flag) {
            while (true) {
                System.out.println("Type in the Search Keyword: ");
                input = scan.nextLine();

                if (checkInputLength(input, 1,30) == false) {
                    //System.out.print("");
                }
                else{
                    flag = false;
                    break;
                }
                }
        }

        try{
            String title_in = input;
            PreparedStatement pre_st = null;
            pre_st = con.prepareStatement("SELECT * FROM book WHERE title = ?");
            pre_st.setString(1, title_in);
            ResultSet title_result = pre_st.executeQuery();

            System.out.println("|Call Num|Title|Book Category|Author|Available No. of Copy|");
            for (boolean flag2 = false; title_result.next(); flag2 = true) {
                use_callnum_getinfo(title_result.getString("callnum"), con, stmt);
                //System.out.println("\n");
            }
            System.out.println("\n");



        }
        catch (SQLException e) {
            System.out.println("[Error]: " + "No matched title found");
        }

    }

    public static void author(Connection con, Statement stmt){
     
        Scanner scan = new Scanner(System.in);
        String input = "A1";
        boolean flag = true;

        while (flag) {
            while (true) {
                System.out.println("Type in the Search Keyword: ");
                input = scan.nextLine();

                if (checkInputLength(input, 0,25) == false) {
                    //System.out.print("");
                }
                else{
                    flag = false;
                    break;
                }
                }
        }

        try{
            String author_in = input;
            PreparedStatement pre_st = null;
            pre_st = con.prepareStatement("SELECT * FROM authorship WHERE aname = ?");
            pre_st.setString(1, author_in);
            ResultSet author_result = pre_st.executeQuery();

            System.out.println("|Call Num|Title|Book Category|Author|Available No. of Copy|");
            for (boolean flag2 = false; author_result.next(); flag2 = true) {
                use_callnum_getinfo(author_result.getString("callnum"), con, stmt);
                //System.out.println("\n");
            }
            System.out.println("\n");



        }
        catch (SQLException e) {
            System.out.println("[Error]: " + e.getMessage());
        }

    }







    public static boolean checkInputLength(String input, int lower_bound, int upper_bound){
        if(input.length() >= lower_bound && input.length() <= upper_bound){
            return true;
        }
        else{
            if(lower_bound == upper_bound){
                System.out.println("[Error]: The input length is [" + lower_bound + "] Character, Please try again ");
            }else{
                System.out.println("[Error]: The input length is [" + lower_bound +"-" + upper_bound + "] Character, Please try again ");
            }
            
            return false;
        }
    }


    public static boolean checkInput(String input, int start, int end) {
        int int_input = 0;

        try {
                int_input = Integer.parseInt(input);
            }
        catch (NumberFormatException e) {
                System.out.println("Please Enter Correct interger: \n");
                return false;
            }

        if (!(int_input > (start-1) && int_input < (end+1))){
            System.out.println("Please Enter Correct number range:\n ");
            return false;
        }
        else{
            return true;
        }

    }

    public static boolean checkInputDate(String input){
        if(input.length() == 10 && input.charAt(2) == '/' && input.charAt(5) == '/'){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                sdf.parse(input);
                return true;
            } catch (ParseException e) {
                System.out.println("[Error]: The date is invalid. Please enter again");
            }
            
        }else{
            System.out.println("[Error]: The date format is wrong. Please enter again. ");
        }
        return false;
    }





}