package DAO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import VO.QnA_Bean;
import utill.ConnectionUtill;
//import utill.ConnectionUtill2;

public class QnA_bMgr {
	//private static final Statement ConnectionUtill = null;
	Connection conn = null;
	Connection conn2 = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	private int curPage;
	private int totPage;
	
	String SAVEFOLDER = "d:/Upload";
	int MAXSIZE = 1024*1024*5;	// 5MB
	String ENCTYPE="euc-kr";
	
	public QnA_bMgr() throws SQLException{
		conn = ConnectionUtill.getConnection();
		//conn2 = ConnectionUtill2.getConnection();
	}  
 
	public int getCurPage() {
		return curPage;
	} 

	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}

	public int getTotPage() {
		return totPage;
	}

	public void setTotPage(int totPage) {
		this.totPage = totPage;
	}

	public QnA_Bean uploadFile( HttpServletRequest request ) throws IOException
	{
		MultipartRequest multi = null;
		QnA_Bean bBean = new QnA_Bean();
		multi = new MultipartRequest(request,
										SAVEFOLDER,
										MAXSIZE, ENCTYPE,
										new DefaultFileRenamePolicy() );			
		
		Enumeration files = multi.getFileNames();
		
		while(files.hasMoreElements())
		{
			String name = (String) files.nextElement();
			bBean.setUploadName( multi.getFilesystemName(name));					
			bBean.setOriginalName( multi.getOriginalFileName( name ));				
			File f=multi.getFile(name);												
			if( f != null )
			{
				bBean.setFileSize( (int) f.length() );
				String type = multi.getContentType(name);
				System.out.println("type : " + type);
			}
			
			bBean.setWriter( multi.getParameter("writer"));
			bBean.setSubject( multi.getParameter("subject"));
			bBean.setPw( multi.getParameter("pw"));
			bBean.setContent( multi.getParameter("content"));


			 if(multi.getParameter("message_num") != null)
			 {
			 	bBean.setNum( Integer.parseInt( ( multi.getParameter( "message_num" ) ) ) );
			 }

			
		}
				
		return bBean;		
	}

	public void downLoad(HttpServletRequest request, HttpServletResponse response,
			JspWriter out, PageContext pageContext) throws IOException {			
		try { 								
			String filename=null;
			String originalName=request.getParameter("originalName");
			filename = URLEncoder.encode(originalName, "utf-8").replaceAll("\\+", "%20");

			System.out.println("download- originalName : "+ originalName);
			System.out.println("download- filename : "+ filename);
			
			File file = new File((SAVEFOLDER + File.separator+ originalName));				
			System.out.println("download- filename1 : "+ file.getName());
			
			byte b[] = new byte[(int) file.length()]; 
			response.setHeader("Accept-Ranges", "bytes");
			response.setHeader("Content-Transfer-Encoding", "binary");
			
			String strClient = request.getHeader("User-Agent");
			//브라우저에 따라 크롬, 파이어폭스인 경우 MSIE방식으로 진행되며 IE의 경우 별도의 방식으로 나뉘어진다//
			if (strClient.indexOf("MSIE") != -1) {					
				response.setContentType("application/smnet;charset=euc-kr");
				response.setHeader("Content-Disposition", "filename=" + filename + ";");
				System.out.println("download- response.setHeader : "+ "Content-Disposition"+ "filename=" + filename + ";");
			} else {					
				
				response.setContentType("application/octer-stream");
				response.setHeader("Content-Disposition", "attachment;filename="+ filename + ";");
				System.out.println("download- response.setHeader : "+ "Content-Disposition"+ "attachment;filename="+ filename + ";");
			}				
			out.clear();
			out=pageContext.pushBody();				
			if (file.isFile()) {
				//파일 다운로드 방식은 그냥 암기하세요 이해할 수 없습니다//
				BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
				BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
				int read = 0;
				while ((read = fin.read(b)) != -1) {
					outs.write(b, 0, read);
				}
				//파일 다 썼으면 버퍼스트림도 다 닫아줘야 한다//
				outs.close();
				fin.close();
				String num=request.getParameter("num");
				dbclose();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	
	public int insertMessage(HttpServletRequest request) {
		StringBuffer sql = new StringBuffer();
		int result = 0;
		try {					 
			QnA_Bean bBean = new QnA_Bean();
			File file = new File(SAVEFOLDER);
			if (!file.exists())
				file.mkdirs();

			bBean=uploadFile(request);		
			
			sql.append("INSERT INTO message04 ");
			//sql.append("VALUES(null,?,?,?,now(),?,0,0)");
			// num, subject, content, writer, pw, date, hit, uploadName, originalName, filSize, downHit, reply
			sql.append( "values (null, ?, ?, ?, ?, now(), 0, ?, ?, ?, 0, 0) ");
//			pstmt = conn.prepareStatement(sql.toString());			
//			pstmt.setString(1, vo.getWriter());	
//			pstmt.setString(2, vo.getSubject());					
//			pstmt.setString(3, vo.getContent());
//			pstmt.setString(4, vo.getPw());
			
			pstmt = conn.prepareStatement( sql.toString() );
			pstmt.setString( 1, bBean.getSubject() );
			pstmt.setString( 2, bBean.getContent() );
			pstmt.setString( 3, bBean.getWriter() );
			pstmt.setString( 4, bBean.getPw() );
			//pstmt.setInt(    5, 0 );										// hit
			pstmt.setString( 5, bBean.getUploadName() );
			pstmt.setString( 6, bBean.getOriginalName() );
			pstmt.setInt(    7, bBean.getFileSize() );
			//pstmt.setInt(    9, 0 );										// down hit
			//pstmt.setInt(    10, 0 );										//
			//pstmt.setInt(    8, vo.getNum() );								//
						
			result = pstmt.executeUpdate();
						
		} catch (Exception e) {
			e.printStackTrace();
			result= -1;
		} finally {
			dbclose();		
		}
		return result;
	}
	
	public String checkPassWord(int num){ 
		System.out.println("�н� üũ message_num : "+ num);
		StringBuffer sql = new StringBuffer();
		String pass= null;
		sql.append("SELECT PW FROM message04 ");
		sql.append("WHERE num=?");
		try{
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				pass = rs.getString("pw");
			}
			System.out.println("num : " +num +" pass : " + pass );
		} catch (Exception ex) {
			System.out.println("Exception" + ex);
		}
		return pass;
	}
	
	
	public int  updatetMessage (HttpServletRequest request) throws IOException{
		System.out.println("updatetMessage 1 : ");
		StringBuffer sql = new StringBuffer();
		QnA_Bean bBean= new QnA_Bean();
		int result = -1;
		System.out.println("updatetMessage 2 : ");
		try{					
			bBean=uploadData(request);
			System.out.println("updateMessage num : " + bBean.getNum() + " pass : " + bBean.getPw() );
			System.out.println("getOriginalDataname 1 : " + bBean.getOriginalName());
			if(checkPassWord(bBean.getNum()).equals(bBean.getPw())){
				sql.append("UPDATE message04 ");
				System.out.println("getOriginalDataname 2" + bBean.getOriginalName());
				if(bBean.getOriginalName()==null){
					sql.append("SET subject=?,content=?,date=now() WHERE num=? ");					
					pstmt = conn.prepareStatement(sql.toString());
					pstmt.setString(1, bBean.getSubject());
					pstmt.setString(2, bBean.getContent());					
					pstmt.setInt(3,bBean.getNum());	
				}else{
					sql.append("SET subject=?,content=?,date=now(),filename=?,origfname=?,filesize=? WHERE num=? ");					
					pstmt = conn.prepareStatement(sql.toString());
					pstmt.setString(1, bBean.getSubject());
					pstmt.setString(2, bBean.getContent());
					pstmt.setString(3, bBean.getUploadName());				
					pstmt.setString(4, bBean.getOriginalName());
					pstmt.setInt(5, bBean.getFileSize());
					pstmt.setInt(6,bBean.getNum());	
				}					
				result = pstmt.executeUpdate();				
			}else{
				//��й�ȣ ����
				result=0;
			}
		}catch(SQLException e){
			System.out.println("updatetMessage try����"); 
			e.printStackTrace();
			result =-1;
		}finally{
			dbclose();
		}
		return result;
	}

	private QnA_Bean uploadData(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public int updatetMessage(QnA_Bean vo){
		StringBuffer sql = new StringBuffer();
		int result = 0;
		try{	
			sql.append("UPDATE message04 ");
			{
				sql.append("SET subject=?,content=?,date=now() WHERE num=? ");					
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setString(1, vo.getSubject());
				pstmt.setString(2, vo.getContent());					
				pstmt.setInt(3, vo.getNum());	
			
				sql.append("SET subject=?,content=?,date=now(),filename=?,origfname=?,filesize=? WHERE num=? ");					
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setString(1, vo.getWriter());
				pstmt.setString(2, vo.getSubject());
				pstmt.setString(3, vo.getContent());					
				pstmt.setString(4, vo.getPw());
				pstmt.setInt(5, vo.getNum());
			}
			
			result = pstmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
			result =-1;
		}finally{
			dbclose();
		}
		return result;
	}
	public QnA_Bean getMessage(int num){
		
		 String sql="select * from message04 where num=?";
		 rs = null;
		 QnA_Bean vo=new QnA_Bean();
		 System.out.println(num);
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if(rs.next()){
				vo.setNum(rs.getInt("num"));
				vo.setWriter(rs.getString("writer"));
				vo.setSubject(rs.getString("subject"));
				vo.setContent(rs.getString("content"));
				System.out.print("content: "+vo.getContent());
				vo.setPw(rs.getString("pw"));
				vo.setHit(rs.getInt("hit"));
				vo.setOriginalName(rs.getString("originalName"));
			}
		} catch (Exception ex) {
			System.out.println("Exception" + ex);
		} finally {     dbclose();}
		return vo;
	}
	
	public int insertReply(QnA_Bean vo){
		StringBuffer sql = new StringBuffer();
		int result=0;
		try {
			sql.append("INSERT INTO message04 ");
//			sql.append("(writer, subject, content, pw, date, hit, num) ");
//			sql.append("VALUES (null,?,?,?,now(),?,0,?) ");
//		
//			pstmt=conn.prepareStatement(sql.toString());
//			pstmt.setString(1, vo.getWriter());
//			pstmt.setString(2, vo.getSubject());
//			pstmt.setString(3, vo.getContent());
//			pstmt.setString(4, vo.getPw());
//			pstmt.setInt(5, vo.getNum());
			
			sql.append( "values (null, ?, ?, ?, ?, now(), 0, ?, ?, ?, 0, ?) ");
			
			pstmt = conn.prepareStatement( sql.toString() );
			pstmt.setString( 1, vo.getSubject() );
			pstmt.setString( 2, vo.getContent() );
			pstmt.setString( 3, vo.getWriter() );
			pstmt.setString( 4, vo.getPw() );
			//pstmt.setInt(    5, 0 );										// hit
			pstmt.setString( 5, vo.getUploadName() );
			pstmt.setString( 6, vo.getOriginalName() );
			pstmt.setInt(    7, vo.getFileSize() );
			pstmt.setInt(    8, vo.getNum() );		
			result = pstmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
			result=-1;
		}finally{
			dbclose();
		}
		return result;
		
	}
	public QnA_Bean getReplyMessage(int id){
		String sql="select * from message04 where num=?";
		rs = null;
		QnA_Bean vo=new QnA_Bean();
		System.out.println(id);
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if(rs.next()){
//				vo.setNum(rs.getInt("num"));
//				vo.setWriter(rs.getString("writer"));
//				vo.setSubject(rs.getString("subject"));
//				vo.setContent(rs.getString("content"));
//				System.out.print("getReplyMessage content: "+vo.getContent());
//				vo.setPw(rs.getString("pw"));
//				vo.setHit(rs.getInt("hit"));
				
				vo.setNum(  rs.getInt("num") );
				vo.setWriter( rs.getString("writer") );
				vo.setSubject( rs.getString("subject") );
				vo.setContent( rs.getString("content") );
				vo.setDate( rs.getString("date") );
				vo.setHit( rs.getInt("hit") );
				vo.setUploadName( rs.getString("uploadName") );
				vo.setOriginalName( rs.getString("originalName") );
				vo.setFileSize( rs.getInt("fileSize") );
				vo.setDownHit( rs.getInt("downHit") );
				vo.setReply( rs.getInt("reply") );				
			}
		}catch (Exception ex){
			System.out.println("Exception"+ ex);
		}finally {  dbclose();}
		return vo;
	}

	private void dbclose() {
		// TODO Auto-generated method stub
	}
	public ArrayList<QnA_Bean> getBoardList(){
		String sql = null;
		ArrayList<QnA_Bean> glist = new ArrayList<QnA_Bean>();
		try{
			sql="SELECT * FROM message04 WHERE reply<1 ORDER BY num DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()){
				QnA_Bean bean = new QnA_Bean();
//				bean.setNum(rs.getInt("num"));
//				bean.setWriter(rs.getString("writer"));
//				bean.setSubject(rs.getString("subject"));
//				bean.setDate(rs.getString("date"));
//				bean.setHit(rs.getInt("hit"));
//				bean.setReply(rs.getInt("reply"));
				
				bean.setNum(  rs.getInt("num") );
				bean.setWriter( rs.getString("writer") );
				bean.setSubject( rs.getString("subject") );
				bean.setContent( rs.getString("content") );
				bean.setDate( rs.getString("date") );
				bean.setHit( rs.getInt("hit") );
				bean.setUploadName( rs.getString("uploadName") );
				bean.setOriginalName( rs.getString("originalName") );
				bean.setFileSize( rs.getInt("fileSize") );
				bean.setDownHit( rs.getInt("downHit") );
				bean.setReply( rs.getInt("reply") );
				
				glist.add(bean);
			}
			sql="SELECT * FROM message04 WHERE reply>1 ORDER BY num DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			int ref=0;
			int i=0;
			while (rs.next()){
				QnA_Bean bean = new QnA_Bean();
//				bean.setNum(rs.getInt("num"));
//				bean.setWriter(rs.getString("writer"));
//				bean.setSubject(rs.getString("subject"));
//				bean.setDate(rs.getString("date"));
//				bean.setHit(rs.getInt("hit"));
				
				bean.setNum(  rs.getInt("num") );
				bean.setWriter( rs.getString("writer") );
				bean.setSubject( rs.getString("subject") );
				bean.setContent( rs.getString("content") );
				bean.setDate( rs.getString("date") );
				bean.setHit( rs.getInt("hit") );
				bean.setUploadName( rs.getString("uploadName") );
				bean.setOriginalName( rs.getString("originalName") );
				bean.setFileSize( rs.getInt("fileSize") );
				bean.setDownHit( rs.getInt("downHit") );				
				
				ref=rs.getInt("reply");
				bean.setReply(ref);
				for(i=0;i<glist.size();i++){
					if(glist.get(i).getNum()==ref) break;
				}
				System.out.println("getBoardList Mgr- i:"+i);
				if(i!=glist.size()){
					while(glist.size() < i){
						glist.addAll(glist.size(), null);
					}
					glist.add(i+1,bean);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			dbclose();
			// TODO Auto-generated method stub
		}
		return glist;
	}
	public int delete(int num) {
		
		StringBuffer sql = new StringBuffer();
		int result = 0;
		try{	
			sql.append("DELETE FROM message04 ");
			sql.append("WHERE num=? ");
			
			pstmt = conn.prepareStatement(sql.toString()); 
			pstmt.setInt(1, num);	
			result = pstmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
			result =-1;
		}finally{
			dbclose();
		}
		return result;		
	}
	public ArrayList<QnA_Bean>getBoardSearchList(String searchType, String word){
		StringBuffer sql=new StringBuffer();
		String sq=null;
		ArrayList<QnA_Bean>glist=new ArrayList<QnA_Bean>();
		try{
			sql.append("SELECT * FROM message04 ");
			if(word != "" && word != null){
				if("all".equals(searchType)){
					sql.append("WHERE subject LIKE '%"+ word + "%"
							+ "OR writer LIKE '%"+ word +"%"
							+ "OR content LIKE '%" + word + "%");
				}else if("subject".equals(searchType)){
					sql.append("WHERE subject LIKE '%" + word + "%");
				}else if("writer".equals(searchType)){
					sql.append("WHERE writer LIKE '%" + word + "%");
				}else if("content".equals(searchType)){
					sql.append("WHERE content LIKE '%" + word + "%");
				}
				sql.append("AND reply<1 ORDER BY num DESC");
				System.out.println("word : "+word+"\t"+"searchType"+"\n");
				System.out.println("sql=="+sql);
			}
			pstmt=conn.prepareStatement(sql.toString());
			rs=pstmt.executeQuery();
			while (rs.next()){
				QnA_Bean bean = new QnA_Bean();
				
//				bean.setNum(rs.getInt("num"));
//				bean.setWriter(rs.getString("writer"));
//				bean.setSubject(rs.getString("subject"));
//				bean.setDate(rs.getString("date"));
//				bean.setHit(rs.getInt("hit"));

				bean.setNum(  rs.getInt("num") );
				bean.setWriter( rs.getString("writer") );
				bean.setSubject( rs.getString("subject") );
				bean.setContent( rs.getString("content") );
				bean.setDate( rs.getString("date") );
				bean.setHit( rs.getInt("hit") );
				bean.setUploadName( rs.getString("uploadName") );
				bean.setOriginalName( rs.getString("originalName") );
				bean.setFileSize( rs.getInt("fileSize") );
				bean.setDownHit( rs.getInt("downHit") );
				bean.setReply( rs.getInt("reply") );
				
				glist.add(bean);
			}
			pstmt=conn.prepareStatement(sq);
			rs = pstmt.executeQuery();
			int ref=0;
			int i=0;
			while(rs.next()){
				QnA_Bean bean = new QnA_Bean();
				
//				bean.setNum(rs.getInt("num"));
//				bean.setWriter(rs.getString("writer"));
//				bean.setSubject(rs.getString("subject"));
//				bean.setDate(rs.getString("date"));
//				bean.setHit(rs.getInt("hit"));

				bean.setNum(  rs.getInt("num") );
				bean.setWriter( rs.getString("writer") );
				bean.setSubject( rs.getString("subject") );
				bean.setContent( rs.getString("content") );
				bean.setDate( rs.getString("date") );
				bean.setHit( rs.getInt("hit") );
				bean.setUploadName( rs.getString("uploadName") );
				bean.setOriginalName( rs.getString("originalName") );
				bean.setFileSize( rs.getInt("fileSize") );
				bean.setDownHit( rs.getInt("downHit") );
				
				ref=rs.getInt("reply");
				bean.setReply(ref);
				for(i=0;i<glist.size();i++){
					if(glist.get(i).getNum()==ref)break;
				}
				System.out.println("getBoardList Mgr- i;"+i);
				if(i!=glist.size()){
					while(glist.size()<i){
						glist.add(glist.size(),null);
					}
					glist.add(i+1,bean);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			dbclose();
		}
		return glist;
	}
}
