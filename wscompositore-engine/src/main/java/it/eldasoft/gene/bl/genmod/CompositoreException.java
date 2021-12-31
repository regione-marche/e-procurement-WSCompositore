/*
 * Created on 25-lug-2006
 *
 * Copyright (c) EldaSoft S.p.A.
 * Tutti i diritti sono riservati.
 *
 * Questo codice sorgente e' materiale confidenziale di proprieta' di EldaSoft S.p.A.
 * In quanto tale non puo' essere distribuito liberamente ne' utilizzato a meno di
 * aver prima formalizzato un accordo specifico con EldaSoft.
 */
package it.eldasoft.gene.bl.genmod;

import java.rmi.RemoteException;

/**
 * Classe per le eccezioni del compositore.
 *
 * @author Marco.Franceschin
 */
public class CompositoreException extends RemoteException {

  // ************************************************************
  // Storia Modifiche:
  // Data Utente Descrizione
  // 12/09/2006 M.F. Eredito da una normale exception perche altrimenti non
  // si riesce a fare il jar scollegato con le librerie
  // 16/10/2006 S.S. Eredito da RemoteException perchè diventa un'eccezione
  // remota; inoltre deve ritornare un codice di errore e non un messaggio
  // ************************************************************

  /**
   * Codici di errori standard
   */
  public static final String CODICE_ERRORE_IN_ESECUZIONE_COMPOSITORE     = "EXECCOMPO";
  public static final String CODICE_ERRORE_POOL_ESAURITO                 = "POOLEMPTY";
  public static final String CODICE_ERRORE_PATH_NON_VALORIZZATO          = "PATHNULL";
  public static final String CODICE_ERRORE_PATH_NON_TROVATO              = "NOPATH";
  public static final String CODICE_ERRORE_CONNECTION_DB_NON_VALORIZZATA = "CONNDBNULL";
  public static final String CODICE_ERRORE_CONNESSIONE                   = "NOCONN";
  public static final String CODICE_ERRORE_MODELLO_NON_TROVATO           = "NOFILECOMPILA";
  public static final String CODICE_ERRORE_MODELLO_FILE_ERRORI_ASSENTE   = "NOFILEERR";
  public static final String CODICE_ERRORE_COMPILAZIONE                  = "COMPIERR";
  public static final String CODICE_ERRORE_MODELLO_NON_COMPILATO         = "NOCOMPILA";
  public static final String CODICE_ERRORE_COMPOSIZIONE                  = "COMPOERR";
  public static final String CODICE_ERRORE_IO                            = "IOFILEERR";
  public static final String CODICE_ERRORE_INASPETTATO_COMPILAZIONE      = "COMPIERRINASP";
  public static final String CODICE_ERRORE_INASPETTATO_COMPOSIZIONE      = "COMPOERRINASP";
  public static final String CODICE_ERRORE_MODO_NON_SUPPORTATO           = "NOTSUPPORTED";

  /**
   * UID
   */
  private static final long  serialVersionUID                            = 462998344456061884L;

  /**
   * Codice di errore univoco che individua l'eccezione all'interno della
   * famiglia
   */
  private String             codiceErrore;

  /**
   * Eventuali parametri da utilizzare per fornire all'utente un messaggio
   * contenente i valori che hanno causato l'eccezione
   */
  private String[]           parametri;

  public String getFamiglia() {
    return "COMPO";
  }

  public CompositoreException(String codiceErrore) {
    super();
    this.codiceErrore = codiceErrore;
    this.parametri = null;
  }

  public CompositoreException(String codiceErrore,String[] parametri) {
    super();
    this.codiceErrore = codiceErrore;
    this.setParametri(parametri);
  }

  /**
   * @return ritorna la chiave da utilizzare per l'accesso ad un resource bundle
   *         per reperire il messaggio di errore da inviare all'utente per la
   *         segnalazione dell'errore
   */
  public String getChiaveResourceBundle() {
    return new StringBuffer(this.getFamiglia()).append(".").append(
        this.codiceErrore).toString();
  }

  /**
   * @return Ritorna codiceErrore.
   */
  public String getCodiceErrore() {
    return codiceErrore;
  }

  /**
   * @return Returns the parametri.
   */
  public String[] getParametri() {
    return this.parametri;
  }

  /**
   * @param parametri
   *        The parametri to set.
   */
  public void setParametri(String[] parametri) {
    this.parametri = parametri;
  }

}
