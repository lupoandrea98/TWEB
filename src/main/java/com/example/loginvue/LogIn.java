package com.example.loginvue;

import com.google.gson.Gson;
import dao.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "login", value = "/login")
public class LogIn extends HttpServlet {
    private final int ID = 0;
    public void init() {
        DAO.registerDriver();
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {

    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        //System.out.println("Richiamata correttamente");
        String action = request.getParameter("action");
        ArrayList<Utente> utenti = Utente.queryDB();
        switch (action) {
            case "login":
                login_post(request, response, utenti);
                break;
            case "sigin":
                sigin_post(request, response, utenti);
                break;
            default:
                System.out.println("action not valid");
        }
    }

    private void sigin_post(HttpServletRequest request, HttpServletResponse response, ArrayList<Utente> utenti) throws IOException{
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String account = request.getParameter("account");
        String pw = request.getParameter("password");
        boolean check_reg = false;
        boolean check_user = false;

        System.out.println("ricevuti " + account + " " + pw );

        if(account != null) {
            Utente exists = Utente.exist(utenti, account, pw);
            if(exists != null) {
                System.out.println("Account già esistente!");
                check_user = true;
                //L'UTENTE ESISTE E DEVO MANDARE AL FRONT L'AVVISO NOME UTENTE ESISTENTE.

            }else {
                //Posso inserire l'utente nel database.
                Utente.insertDB(account, pw, 0);
                check_reg = true;
            }

        }
        ArrayList<Boolean> checks = new ArrayList<Boolean>();
        checks.add(check_user);
        checks.add(check_reg);
        try{
            Gson gson = new Gson();
            String checkString = gson.toJson(checks);
            System.out.println(checkString);
            out.println(checkString);
        }finally {
            out.flush();
            out.close();
        }

    }

    private void login_post(HttpServletRequest request, HttpServletResponse response, ArrayList<Utente> utenti) throws IOException{
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String account = request.getParameter("account");
        String pw = request.getParameter("password");
        boolean check = false;
        System.out.println("ricevuti " + account + " " + pw);
        HttpSession session = request.getSession();
        System.out.println("Sessione attuale = " + session.getAttribute("ruolo"));
        if(account != null) {
            Utente exists = Utente.exist(utenti, account, pw);
            if(exists != null) {  //Se l'account esiste allora imposto gli attributi con i suoi valori

                request.setAttribute("account", account);
                request.setAttribute("password", pw);
                session.setAttribute("ruolo", exists.getRuolo());
                System.out.println(account + " ha loggato come " + exists.getAdmin());

                check = true;
            }else{                //Altrimenti imposto delle stringhe vuote
                request.setAttribute("account", "");
                request.setAttribute("password", "");
                session.setAttribute("ruolo", "ospite");
                System.out.println(account + " non è registrato");
            }
        }else {
            System.out.println("account parameter null");
        }
        //Passo alla pagina una risposta tramite Json.
        try{
            Gson gson = new Gson();
            String checkString = gson.toJson(check);
            System.out.println(checkString);
            out.println(checkString);
        }finally {
            out.flush();
            out.close();
        }

        System.out.println("Ruolo di sessione memorizzato = " + session.getAttribute("ruolo"));

    }

    private void insertDoc(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Boolean success = false;
        HttpSession session = request.getSession();
        System.out.println("Ruolo di sessione "+(String)session.getAttribute("ruolo"));
        if(session.getAttribute("ruolo") == null)
            System.out.println("Ruolo di sessione non presente, impossibile procedere con le operazioni desiderate");

        if(session.getAttribute("ruolo").equals("admin")) {

            String nome = request.getParameter("nome");
            String cognome = request.getParameter("cognome");

            if(nome != null && cognome != null) {
                Docente.insertDB(nome, cognome);
                success = true;
            }else{
                System.out.println("Parametri dal form nulli");
            }
        }else
            System.out.println("Non sei amministratore figlio di puttana, muori male");

        //response per avvertire l'utente del fallimento nell'inserire un nuovo docente.
        PrintWriter out = response.getWriter();
        try {
            response.setContentType("text/plain");
            if(success) out.println("Docente inserito correttamente");
            else out.println("Impossibile inserire il docente");
            out.flush();
        }finally {
            out.close();
        }
    }

    private void postJSJquery(HttpServletRequest request, HttpServletResponse response)throws IOException {
        String account = request.getParameter("account");
        String pw = request.getParameter("password");
        boolean success = false;

        HttpSession session = request.getSession();
        System.out.println("Sessione attuale = " + session.getAttribute("ruolo"));
        if(account != null) {
            ArrayList<Utente> utenti = Utente.queryDB();
            Utente exist = Utente.exist(utenti, account, pw);
            if(exist != null) {

                session.setAttribute("ruolo", exist.getRuolo());
                success = true;
                System.out.println(account + " ha loggato come " + exist.getAdmin());

            }else {
                session.setAttribute("ruolo", "ospite");
                System.out.println(account + " non è registrato");
                success = true;
            }
        }else {
            System.out.println("Il parametro account è nullo");
        }
        //response per genereare un alert sul successo o fallimento dell'operazione di login.
        PrintWriter out = response.getWriter();
        try {
            response.setContentType("text/plain");
            if(success) out.println("log in avvenuto con successo");
            else out.println("log in fallito");
            out.flush();
        }finally {
            out.close();
        }

        System.out.println("Ruolo di sessione memorizzato = " + session.getAttribute("ruolo"));

    }

}