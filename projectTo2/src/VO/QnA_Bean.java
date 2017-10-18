package VO;

public class QnA_Bean {

//	"num int(11) not null primary key auto_increment, " +
//  "subject nvarchar (50), " +
//  "content text, " +
//  "writer nvarchar(20), " +													   
//  "pw nvarchar(20), " +
//  "date datetime, " + 													   
//  "hit smallint(7) unsigned, " +
//  "uploadName nvarchar(200), " +
//  "originalName nvarchar(200), " +
//  "fileSize int(11), " +
//  "downHit int(11), "
//	"reply int(11)"

	private int    num;
	private String subject;
	private String content;	
	private String writer;
	private String pw;
	private String date;
	private int    hit;					
	private String uploadName;	
	private String originalName;
	private int    fileSize;
	private int    downHit;
	private int    reply;				 
										
	
	public int getNum() {
		return num;
	}
	
	public void setNum(int num) {
		this.num = num;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getWriter() {
		return writer;
	}
	
	public void setWriter(String writer) {
		this.writer = writer;
	}
	
	public String getPw() {
		return pw;
	}
	
	public void setPw(String pw) {
		this.pw = pw;
	}
	
	public String getDate() {
	
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public int getHit() {
		return hit;
	}
	
	public void setHit(int hit) {
		this.hit = hit;
	}
	
	public String getUploadName() {
		return uploadName;
	}
	
	public void setUploadName(String uploadName) {
		this.uploadName = uploadName;
	}
	
	public String getOriginalName() {
		return originalName;
	}
	
	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
	
	public int getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	
	public int getDownHit() {
		return downHit;
	}
	
	public void setDownHit(int downHit) {
		this.downHit = downHit;
	}

	public int getReply() {
		return reply;
	}

	public void setReply(int reply) {
		this.reply = reply;
	}

}
