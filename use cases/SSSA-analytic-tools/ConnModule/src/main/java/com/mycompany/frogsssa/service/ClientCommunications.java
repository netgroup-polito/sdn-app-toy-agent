///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.mycompany.frogsssa.service;
//
//import com.mycompany.frogsssa.Message;
//import info.macias.sse.EventTarget;
//import info.macias.sse.servlet3.ServletEventTarget;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.Map;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.LinkedBlockingQueue;
//import javax.servlet.AsyncContext;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
///**
// *
// * @author lara
// */
//@WebServlet(asyncSupported = true)
//public class ClientCommunications extends HttpServlet {
//
////    private Map<Long, AsyncContext> asyncContexts = new ConcurrentHashMap<>();
////    private BlockingQueue<Message> msgQueue = new LinkedBlockingQueue<>();
////    private boolean running = false;
////    
//        static EventTarget target;
//    
////    private Thread notifier = new Thread(new Runnable() {
////        @Override
////        public void run() {
////            while(running){
////                try{
////                    Message m = msgQueue.take();
////                    if(asyncContexts.containsKey(m.getIdClient())){
////                        sendMessage(asyncContexts.get(m.getIdClient()).getResponse().getWriter(), m.getMessage());
////                    }
////                }catch(Exception e){
////                    System.err.println("Exception " + e.getMessage());
////                }
////            }
////        }
////
////        private void sendMessage(PrintWriter writer, String message) {
////            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////        }
////    });
////    
////    public void insertNewMessage(Long id, String message){
////        Message m = new Message(message, id);
////        msgQueue.add(m);
////    }
//    
//        // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
//    /**
//     * Handles the HTTP <code>GET</code> method.
//     *
//     * @param request servlet request
//     * @param response servlet response
//     * @throws ServletException if a servlet-specific error occurs
//     * @throws IOException if an I/O error occurs
//     */
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        target = new ServletEventTarget(request).ok().open();
//    }
//
//    /**
//     * Handles the HTTP <code>POST</code> method.
//     *
//     * @param request servlet request
//     * @param response servlet response
//     * @throws ServletException if a servlet-specific error occurs
//     * @throws IOException if an I/O error occurs
//     */
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        
//    }
//    
//    public static void sendMessage(String message) throws IOException {
//        target.send("title", message);
//    }
//    
//    /**
//     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
//     * methods.
//     *
//     * @param request servlet request
//     * @param response servlet response
//     * @throws ServletException if a servlet-specific error occurs
//     * @throws IOException if an I/O error occurs
//     */
//    /*protected void processRequest(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        response.setContentType("text/html;charset=UTF-8");
//        try (PrintWriter out = response.getWriter()) {
//            /* TODO output your page here. You may use following sample code. */
//            /*out.println("<!DOCTYPE html>");
//            out.println("<html>");
//            out.println("<head>");
//            out.println("<title>Servlet ClientCommunications</title>");            
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<h1>Servlet ClientCommunications at " + request.getContextPath() + "</h1>");
//            out.println("</body>");
//            out.println("</html>");
//        }
//    }*/
//
//    /**
//     * Returns a short description of the servlet.
//     *
//     * @return a String containing servlet description
//     */
//    @Override
//    public String getServletInfo() {
//        return "Short description";
//    }// </editor-fold>
//
//}
