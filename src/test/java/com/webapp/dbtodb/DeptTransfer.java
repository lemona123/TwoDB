package com.webapp.dbtodb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webapp.dao.DeptDao;
import com.webapp.exception.InsertRuntimeException;
import com.webapp.model.Dept;

public class DeptTransfer {

   /*
   * Oracle 
   */
   static String oclassName = "oracle.jdbc.OracleDriver";
   static String ourl = "jdbc:oracle:thin:@localhost:1521:xe";
   static String ouser = "scott";
   static String opassword = "tiger";
      
   /*
   * Mysql
   */
   static String mclassName = "com.mysql.jdbc.Driver"; 
   static String murl = "jdbc:mysql://localhost:3306/world"; 
   static String muser = "world";
   static String mpassword = "1234";   

   static Log log = LogFactory.getLog(DeptTransfer.class); // 이거 임포트할 때 mysql 아니고 commonslog임
   static void classLoader(){
      
      try {
         Class.forName(oclassName);
         Class.forName(mclassName);
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
         System.exit(-1);
      }
   }
   
   public static void main(String[] args) {
      classLoader();
      
      Connection oracle=null;
      Connection mysql = null;
      
      try {
          oracle = DriverManager.getConnection(ourl, ouser, opassword);
          mysql = DriverManager.getConnection(murl, muser, mpassword);
          
          
          /*
           * Oracle Dept select All
           */
          
          DeptDao dao = new DeptDao(oracle);
          List<Dept> depts=dao.selectAll();
          
          for (Dept d : depts){
             log.info(d.getDeptno()+" "+d.getDname()+" "+d.getLoc());
          }
                          
          /*
           * Mysql Dept select All
           */
          
          mysql.setAutoCommit(false);
          dao.setConnection(mysql); // 커넥션 바꿔주기
          // 요기서 트랜잭션이 필요하다. (올 오아 나띵) -> 오토커밋 펄스로 풀어주기
          
          try{
             dao.deleteAll();
             for (Dept d: depts){
                dao.insert(d); // 여기서 익셉션 발생했다면? 을 체크해야 (이거 때문에 익셉션 클래스를 따로 하나 만들어준거임 (패키지도 따로))
             }
             mysql.commit(); // 익셉션 발생 안한경우 -> 커밋
             log.info("Program normal exit..."); // 정상종료 (로딩되었음)
             
             depts = dao.selectAll();
             for (Dept d : depts){
                  log.info(d.getDeptno()+" "+d.getDname()+" "+d.getLoc());
               }
             
          }catch (InsertRuntimeException e){
             mysql.rollback(); // 발생한 경우 -> 롤백해줌
             log.info("mysql insert error...", e);
          }

      } catch (SQLException e) {
         log.info("Connection fail...");
      } finally{
         try {
            if(oracle!=null)oracle.close();
            if(mysql!=null)mysql.close();
         } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }
}