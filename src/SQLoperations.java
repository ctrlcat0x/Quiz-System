import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLoperations {
	
	Connection con;
	
	// Database configuration - update these values for your environment
	private static final String DB_URL = "jdbc:mysql://localhost:3306/survey";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "YOUR_PASSWORD_HERE"; // Change to your MySQL password
	
	public SQLoperations() throws SQLException {
		con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
	}
	
	public void newUser(String name, String uname, String pass) throws SQLException {
		String str = "INSERT INTO actors(fname, uname, pass) VALUES (?, ?, ?)";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, name);
			pst.setString(2, uname);
			pst.setString(3, pass);
			pst.executeUpdate();
		}
	}
	
	public int authUser(String uname, String pass) throws SQLException {
		String str = "SELECT * FROM actors WHERE uname = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, uname);
			try (ResultSet rst = pst.executeQuery()) {
				if (!rst.next())
					return -1;
				else {
					if (rst.getString("pass").equals(pass))
						return rst.getInt("id");
					else
						return 0;
				}
			}
		}
	}
	
	public void newQuestion(String code, String question, String op1, String op2, String op3, String op4) throws SQLException {
		String str = "INSERT INTO questions VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, code);
			pst.setString(2, question);
			pst.setString(3, op1);
			pst.setString(4, op2);
			pst.setString(5, op3);
			pst.setString(6, op4);
			pst.executeUpdate();
		}
	}
	
	public void userQuestionAdd(int id, String quizcode) throws SQLException {
		String str = "INSERT INTO userQuestions VALUES (?, ?, 0)";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setInt(1, id);
			pst.setString(2, quizcode);
			pst.executeUpdate();
		}
	}
	
	public void answerUpdt(String quizcode, int qno, int option) throws SQLException {
		String str = "INSERT INTO quizquestions VALUES (?, ?, ?)";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, quizcode);
			pst.setInt(2, qno);
			pst.setInt(3, option);
			pst.executeUpdate();
		}
	}
	
	public ResultSet getQuestions(String quizcode) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT * FROM questions WHERE quizcode = ?");
		pst.setString(1, quizcode);
		return pst.executeQuery();
	}
	
	public ResultSet surveys(int id, String search) throws SQLException {
		PreparedStatement pst = con.prepareStatement("SELECT * FROM userQuestions WHERE id = ? AND quizcode LIKE ?");
		pst.setInt(1, id);
		pst.setString(2, "%" + search + "%");
		return pst.executeQuery();
	}
	
	public void addTotal(String quizcode) throws SQLException {
		String str = "UPDATE userQuestions SET total = total + 1 WHERE quizcode = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, quizcode);
			pst.executeUpdate();
		}
	}
	
	public boolean check(String search) throws SQLException {
		String str = "SELECT * FROM userQuestions WHERE quizcode = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, search);
			try (ResultSet rst = pst.executeQuery()) {
				return rst.next();
			}
		}
	}
	
	public void removeSurvey(String quizcode) throws SQLException {
		try (PreparedStatement pst1 = con.prepareStatement("DELETE FROM questions WHERE quizcode = ?");
		     PreparedStatement pst2 = con.prepareStatement("DELETE FROM quizquestions WHERE quizcode = ?");
		     PreparedStatement pst3 = con.prepareStatement("DELETE FROM userQuestions WHERE quizcode = ?")) {
			pst1.setString(1, quizcode);
			pst1.executeUpdate();
			pst2.setString(1, quizcode);
			pst2.executeUpdate();
			pst3.setString(1, quizcode);
			pst3.executeUpdate();
		}
	}
	
	public int getCount(String quizcode, int qno, int op) throws SQLException {
		String str = "SELECT COUNT(opno) AS cnt FROM quizquestions WHERE quizcode = ? AND qno = ? AND opno = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, quizcode);
			pst.setInt(2, qno + 1);
			pst.setInt(3, op);
			try (ResultSet rst = pst.executeQuery()) {
				if (rst.next())
					return rst.getInt("cnt");
				else
					return 0;
			}
		}
	}
	
	public boolean checkUsername(String uname) throws SQLException {
		String str = "SELECT id FROM actors WHERE uname = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, uname);
			try (ResultSet rst = pst.executeQuery()) {
				return rst.next();
			}
		}
	}
	
	public void changePassword(int id, String newPass) throws SQLException {
		String str = "UPDATE actors SET pass = ? WHERE id = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, newPass);
			pst.setInt(2, id);
			pst.executeUpdate();
		}
	}
	
	public String getUsername(int id) throws SQLException {
		String str = "SELECT uname FROM actors WHERE id = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setInt(1, id);
			try (ResultSet rst = pst.executeQuery()) {
				if (rst.next())
					return rst.getString("uname");
				else
					return null;
			}
		}
	}
	
	public int getTotal(String quizcode) throws SQLException {
		String str = "SELECT total FROM userQuestions WHERE quizcode = ?";
		try (PreparedStatement pst = con.prepareStatement(str)) {
			pst.setString(1, quizcode);
			try (ResultSet rst = pst.executeQuery()) {
				if (rst.next())
					return rst.getInt("total");
				else
					return 0;
			}
		}
	}
	
	public void close() {
		try {
			if (con != null && !con.isClosed())
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
