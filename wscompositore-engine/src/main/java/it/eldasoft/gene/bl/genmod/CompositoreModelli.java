/*
 * Created on 28-giu-2006
 *
 * Copyright (c) Maggioli S.p.A.
 * Tutti i diritti sono riservati.
 *
 * Questo codice sorgente e' materiale confidenziale di proprieta' di Maggioli S.p.A.
 * In quanto tale non puo' essere distribuito liberamente ne' utilizzato a meno di
 * aver prima formalizzato un accordo specifico con Maggioli.
 */
package it.eldasoft.gene.bl.genmod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Classe di business che esegue la composizione di un modello
 *
 * @author Marco.Franceschin
 */
public class CompositoreModelli {

  private static final String ENTITA_CHIAVE_FITTIZIA_PER_DATASOURCE_XML = "NON_USATO:N=0";

  static Logger                  logger                = Logger.getLogger(CompositoreModelli.class);

  /** Proprietà in cui è scritta il nome del compositore esterno */
  public static final String     PROP_PATH_COMPO       = "compo.path";

  /** Stringa di connessione al database * */
  private String                 connectString;

  /** Path di accesso ai modelli */
  private String                 pathModelli;

  /** Path output dei modelli */
  private String                 pathOutput;

  /**
   * Nome del file, presente nella cartella pathModelli, da passare al
   * compositore
   */
  private String                 nomeModello;

  /**
   * Nome del file, presente nella cartella pathModelli/out, contenente i dati
   * da utilizzare per la composizione del modello
   */
  private String                 nomeFileSorgenteDati;

  /** Entità interessata alla generazione */
  private String                 entita;

  /** Elenco dei mnemonici dei campi chiave dell'entità */
  private String[]               campiChiave;

  /**
   * Elenco dei valori dei campi chiave per l'elemento dell'entità su cui
   * eseguire l'elaborazione
   */
  private String[]               valoriChiave;

  /**
   * SQL per la selezione dell'entità sul database. Attenzione: nella selezione
   * devono essere compresi i campi chiave altrimenti non funziona
   */
  private String                 sqlDiSelezione;

  /**
   * Registri da inoltrare al compositore
   */
  private String                 registri;

  /** true se si utilizza il compositore java, false se si utilizza il vecchio compositore legacy C (ODBC o JDBC). */
  private boolean                isCompositoreJava;

  /** Path del compositore da porre in esecuzione. */
  private String                 pathCompositore;

  /**
   * Pool che gestisce l'elenco di compositori
   */
  private PoolCompositoreModelli pool;

  /**
   * Tipo di database secondo codifica interna Eldasoft:
   * <ul>
   * <li><b>ORA</b>: Oracle</li>
   * <li><b>MSQ</b>: SqlServer</li>
   * <li><b>POS</b>: PostgreSQL</li>
   * </ul>
   */
  private String                 dbms;

  /**
   * Tipo di database, vecchio codice per Compositore eseguibile:
   * <ul>
   * <li><b>O</b>: Oracle</li>
   * <li><b>M</b>: SqlServer</li>
   * <li><b>P</b>: PostgreSQL</li>
   * </ul>
   */
  private char                   tipoDB;

  /**
   * Nome del file prodotto come risultato
   */
  private String                 fileComposto;

  /**
   * Costruttore vuoto
   */
  public CompositoreModelli(PoolCompositoreModelli pool) {
    this.reset();
    this.pool = pool;
  }

  /**
   * Svuota il contenuto dell'oggetto
   */
  protected void reset() {
    this.connectString = null;
    this.pathModelli = null;
    this.pathOutput = null;
    this.nomeModello = null;
    this.nomeFileSorgenteDati = null;
    this.entita = null;
    this.campiChiave = null;
    this.valoriChiave = null;
    this.sqlDiSelezione = null;
    this.dbms = null;
    this.tipoDB = ' ';
    this.fileComposto = null;
    this.registri = null;
    this.isCompositoreJava = false;
    this.pathCompositore = null;
  }

  /**
   * @return Ritorna campiChiave.
   */
  public String[] getCampiChiave() {
    return campiChiave;
  }

  /**
   * @param campiChiave
   *        campiChiave da settare internamente alla classe.
   */
  public void setCampiChiave(String[] campiChiave) {
    this.campiChiave = campiChiave;
    this.sqlDiSelezione = null;

  }

  /**
   * Funzione che setta i campi chiave partendo da un elenco diviso da ;
   *
   * @param campiChiave
   */
  public void setCampiChiave(String campiChiave) {
    this.setCampiChiave(CompositoreModelli.stringToArray(campiChiave));
  }

  /**
   * @return Ritorna entita.
   */
  public String getEntita() {
    return entita;
  }

  /**
   * @param entita
   *        entita da settare internamente alla classe.
   */
  public void setEntita(String entita) {
    this.entita = entita;
    this.sqlDiSelezione = null;
  }

  /**
   * @return Ritorna nomeModello.
   */
  public String getNomeModello() {
    return nomeModello;
  }

  /**
   * @param nomeModello
   *        nomeModello da settare internamente alla classe.
   */
  public void setNomeModello(String nomeModello) {
    this.nomeModello = nomeModello;
  }

  /**
   * @return Ritorna nomeFileSorgenteDati.
   */
  public String getNomeFileSorgenteDati() {
    return nomeFileSorgenteDati;
  }

  /**
   * @param nomeFileSorgenteDati nomeFileSorgenteDati da settare internamente alla classe.
   */
  public void setNomeFileSorgenteDati(String nomeFileSorgenteDati) {
    this.nomeFileSorgenteDati = nomeFileSorgenteDati;
    this.entita = null;
    this.campiChiave = null;
    this.valoriChiave = null;
  }

  /**
   * @return Ritorna pathModelli.
   */
  public String getPathModelli() {
    return pathModelli;
  }

  /**
   * @param pathModelli
   *        pathModelli da settare internamente alla classe.
   */
  public void setPathModelli(String pathModelli) {
    this.pathModelli = pathModelli;
  }

  /**
   * @return Ritorna pathOutput.
   */
  public String getPathOutput() {
    return pathOutput;
  }

  /**
   * @param pathOutput
   *        pathOutput da settare internamente alla classe.
   */
  public void setPathOutput(String pathOutput) {
    this.pathOutput = pathOutput;
  }

  /**
   * @return Ritorna valoriChiave.
   */
  public String[] getValoriChiave() {
    return valoriChiave;
  }

  /**
   * @param valoriChiave
   *        valoriChiave da settare internamente alla classe.
   */
  public void setValoriChiave(String[] valoriChiave) {
    this.valoriChiave = valoriChiave;
    this.sqlDiSelezione = null;

  }

  /**
   * Funzione che setta i valori dei campi chiave prendendoli da una stringa
   * suddivisa da ;
   *
   * @param valoriSuddivisi
   *        Stringa con i valori suddivisi da ;
   */
  public void setValoriChiave(String valoriSuddivisi) {
    this.setValoriChiave(CompositoreModelli.stringToArray(valoriSuddivisi));
  }

  /**
   * @return Ritorna sqlDiSelezione.
   */
  public String getSqlDiSelezione() {
    return sqlDiSelezione;
  }

  /**
   * @param sqlDiSelezione
   *        sqlDiSelezione da settare internamente alla classe.
   */
  public void setSqlDiSelezione(String sqlDiSelezione) {
    this.sqlDiSelezione = sqlDiSelezione;
    this.entita = null;
    this.campiChiave = null;
    this.valoriChiave = null;
  }

  /**
   * @return Ritorna dbms.
   */
  public String getDbms() {
    return dbms;
  }

  /**
   * @param dbms dbms da settare internamente alla classe.
   */
  public void setDbms(String dbms) {
    this.dbms = dbms;
  }

  /**
   * @param tipoDB
   *        tipoDB da settare internamente alla classe.
   */
  public void setTipoDB(char tipoDB) {
    this.tipoDB = tipoDB;
  }

  /**
   * @return Ritorna tipoDB.
   */
  public char getTipoDB() {
    return tipoDB;
  }

  /**
   * @return Ritorna fileComposto.
   */
  public String getFileComposto() {
    return fileComposto;
  }

  /**
   * @param fileComposto
   *        fileComposto da settare internamente alla classe.
   */
  private void setFileComposto(String fileComposto) {
    this.fileComposto = fileComposto;
  }

  /**
   * @return Ritorna contesto.
   */
  public String getRegistri() {
    return registri;
  }

  /**
   * @param contesto contesto da settare internamente alla classe.
   */
  public void setRegistri(String registri) {
    this.registri = registri;
    if (!this.isCompositoreJava()) {
      // l'eventuale registro 87 va aggiunto implicitamente esclusivamente per il vecchio compositore eseguibile, mentre non va
      // assolutamente aggiunto nel nuovo compositore java in quanto il valore passato viene utilizzato per filtrare i tabellati in TAB1
      // mediante la colonna app_prefix (usata e presente solo in TAB1 su SICRAWEB)
      if (StringUtils.isNotEmpty(this.registri) && !StringUtils.contains(this.registri,"87=")) {
        // si aggiunge implicitamente il registro 87 se non giunge dalla chiamata
        this.registri += "87=pwb;";
      }
    }
    this.registri = StringUtils.chomp(this.registri, ";");
  }

  /**
   * @return Ritorna isCompositoreJava.
   */
  public boolean isCompositoreJava() {
    return isCompositoreJava;
  }

  /**
   * @param isCompositoreJava isCompositoreJava da settare internamente alla classe.
   */
  public void setCompositoreJava(boolean isCompositoreJava) {
    this.isCompositoreJava = isCompositoreJava;
  }

  /**
   * @return Ritorna pathCompositore.
   */
  public String getPathCompositore() {
    return pathCompositore;
  }

  /**
   * @param pathCompositore pathCompositore da settare internamente alla classe.
   */
  public void setPathCompositore(String pathCompositore) {
    this.pathCompositore = pathCompositore;
  }

  /**
   * Funzione che esegue la composizione di un testo
   *
   * @param currentRow
   *        Riga corrente. La riga parte da in poi
   * @param lastRow
   *        Flag che dice che si tratta dell'ultima riga
   * @param generaPdf
   *        false se si vuole generare il documento con la stessa estensione del modello in input, true se si vuole ritornare un pdf
   * @return true se la composizione viene eseguita correttamente, altrimenti
   *         false
   * @throws CompositoreException
   *         Eccezione del compositore
   */
  public boolean componi(int currentRow,
      boolean lastRow, boolean generaPdf) throws CompositoreException {

    boolean esito = true;

    if (logger.isDebugEnabled()) logger.debug("componi: inizio metodo");

    boolean continua = true;
    String lsFile = null;
    String lsOut = null;
    String lsFileXml = null;
    String lsComposto = null;
    String lsPartenza = null;
    Calendar lCamend = null;
    CompositoreException ce = null;
    String error = null;
    String message = null;
    File fileXmlOrigine = null;

    try {

      // Sabbadin 25/03/2010: introdotto il nome del file xml sorgente dati,
      // pertanto viene definito un reference al file per effettuare le
      // successive eliminazioni dello stesso
      if (this.getNomeFileSorgenteDati() != null)
        fileXmlOrigine = new File(this.getPathOutput()
            + this.getNomeFileSorgenteDati());

      if (continua) {
        lsFile = this.getPathModelli() + this.getNomeModello();
      }

      if (continua) {
        // si procede alla composizione del documento
        lCamend = Calendar.getInstance();
        // {MF230807} Trasformo il nome del documento da compilato a documento
        // Sabbadin 21/10/2014: il documento composto prende il nome del modello e con un timestamp leggibile
        lsOut = this.getPathOutput()
            + StringUtils.substringBeforeLast(this.nomeModello, ".")
            + "_"
            + StringUtils.remove(StringUtils.remove(DateFormatUtils.ISO_DATETIME_FORMAT.format(lCamend), ':'), '-')
            + RandomStringUtils.randomNumeric(3);
        lsComposto = "";

        if (this.getSqlDiSelezione() != null) {
          if (logger.isDebugEnabled())
            logger.debug("Composizione: "
                + lsFile
                + " --> "
                + lsOut
                + " sql = "
                + this.getSqlDiSelezione());

          lsComposto = this.componiFromSQL(lsFile, lsOut,
              this.getSqlDiSelezione());
        } else if (fileXmlOrigine != null) {
          if (logger.isDebugEnabled())
            logger.debug("Composizione: "
                + lsFile
                + ", sorgente dati: "
                + this.getNomeFileSorgenteDati()
                + " --> "
                + lsOut);

          // il file xml effettivamente inviato al compositore ha lo stesso nome
          // e path del file di output per la composizione, eccetto
          // l'estensione, in modo da non aggiungere ulteriori parametri alla
          // chiamata al compositore
          lsFileXml = lsOut + ".xml";
          try {
            FileUtils.moveFile(fileXmlOrigine, new File(lsFileXml));
            lsComposto = this.componi(lsFile, lsOut, ENTITA_CHIAVE_FITTIZIA_PER_DATASOURCE_XML,
                currentRow + 1, lastRow, currentRow > 0, generaPdf);
          } catch (IOException e) {
            message = e.getMessage();
            error = CompositoreException.CODICE_ERRORE_INASPETTATO_COMPOSIZIONE;
            logger.error(
                "Errore inaspettato durante la modifica del nome al file xml sorgente dati del modello",
                e);
          }

        } else {
          // Si tratta della composizione con entità e campi chiave
          lsPartenza = this.getEntita() + ":";
          for (int i = 0; i < this.getCampiChiave().length; i++) {
            if (i > 0) lsPartenza += ";";
            lsPartenza += this.getCampiChiave()[i] + "=";
            lsPartenza += CompositoreModelli.aggiungiBackslashPrimaDiPuntoVirgolaEBackslash(this.getValoriChiave()[i]);
          }
          if (currentRow > 0) {
            lsOut = this.getFileComposto();
            // Elimino l'estensione
            if (lsOut.indexOf(".") >= 0) {
              lsOut = lsOut.substring(0, lsOut.lastIndexOf("."));
            }
          }
          if (logger.isDebugEnabled())
            logger.debug("Composizione: "
                + lsFile
                + " --> "
                + lsOut
                + "("
                + lsPartenza
                + ") last:"
                + lastRow
                + " currentRow:"
                + currentRow
                + " append:"
                + (currentRow > 0));
          lsComposto = this.componi(lsFile, lsOut, lsPartenza,
              currentRow + 1, lastRow, currentRow > 0, generaPdf);
        }

        // Imposto il path del file composto
        this.setFileComposto(lsComposto);
        esito = !lsComposto.substring(lsComposto.length() - 1).equals("!");

        if (logger.isDebugEnabled())
          logger.debug("composto="
              + lsComposto
              + ", esito="
              + (esito ? "vero" : "falso"));
      }

      if (continua && error == null && !esito) {
        // Eseguo la lettura del file errori
        String pathErr = lsOut + ".err";
        if (logger.isDebugEnabled())
          logger.debug("Leggo l'errore dal file: " + pathErr);
        File fileErr = new File(pathErr);
        if (fileErr.exists()) {
          FileReader fileReader = null;
          try {
            fileReader = new FileReader(pathErr);
            char[] buffer = new char[(int) fileErr.length()];
            fileReader.read(buffer);
            message = String.valueOf(buffer).trim();
            error = CompositoreException.CODICE_ERRORE_COMPOSIZIONE;
          } catch (Throwable t) {
            message = t.getMessage();
            error = CompositoreException.CODICE_ERRORE_IO;
            logger.error(
                "Errore inaspettato durante la lettura del file degli errori di ritorno dal compositore",
                t);
          } finally {
            if (fileReader != null) {
              try {
                fileReader.close();
              } catch (Throwable t) {
              }
            }
          }
        } else {
          error = CompositoreException.CODICE_ERRORE_MODELLO_FILE_ERRORI_ASSENTE;
        }

        // si elimina l'eventuale file composto (potrebbe essere parziale), e
        // tutti i file correlati alla composizione (file di errori, file xml,
        // ...)
        this.eliminaFile(lsOut.concat(this.getNomeModello().substring(
            this.getNomeModello().lastIndexOf('.'))));

        // si termina con un errore che indica che la composizione è
        // fallita
        ce = new CompositoreException(error);
        if (message != null) ce.setParametri(new String[] { message });
        continua = false;
      }

      if (!continua && ce != null) throw ce;

      if (logger.isDebugEnabled()) logger.debug("componi: fine metodo");

    } finally {
      // se la ricompilazione fallisce, si tenta di eliminare il file XML
      // origine dati se definito per la composizione
      if (fileXmlOrigine != null && fileXmlOrigine.exists())
        fileXmlOrigine.delete();

      this.pool.setCompositore(this);
    }

    return esito;
  }

  /**
   * Esecuzione del compositore da eseguibile esterno P.S. Per i parametri e il
   * tipo di comando vedere l'eseguibile compositore esterno
   *
   * @param pathCompositore path al compositore (eseguibile o batch)
   * @param asCmd
   *        Tipo di comando
   * @param asParams
   *        Parametri per il comando
   * @param ret
   *        Buffer che conterrà l'eventuale errore o il file d'uscita
   * @return
   */
  private int eseguiCompo(String pathCompositore, String asCmd, String asParams[], StringBuffer ret)
      throws CompositoreException {
    Vector<String> vPar = new Vector<String>();
    String lsExt = "";
    if (pathCompositore.indexOf('.') > 0)
      lsExt = pathCompositore.substring(pathCompositore.lastIndexOf('.'));
    if (".cmd".equalsIgnoreCase(lsExt)) {
      // Si tratta di un comando cms
      vPar.add("cmd.exe");
      vPar.add("/C");
    }
    vPar.add(pathCompositore);
    vPar.add(asCmd);
    for (int i = 0; i < asParams.length; i++) {
      if (asParams[i] != null) vPar.add(asParams[i]);
    }
    String lsParm[] = new String[vPar.size()];
    for (int i = 0; i < vPar.size(); i++)
      lsParm[i] = vPar.get(i);
    try {
      String line;
      String workDir = pathCompositore.substring(0, pathCompositore.lastIndexOf('/'));
      if (logger.isDebugEnabled()) {
        StringBuffer buf = new StringBuffer("");
        buf.append("Sta per essere richiamato il compositore: ");
        buf.append(asCmd);
        buf.append(" pars( ");
        buf.append(vPar.toString());
        logger.debug(buf.toString());
      }
      Process proc = Runtime.getRuntime().exec  (lsParm, null, new File(workDir));
      BufferedReader lRet = new BufferedReader(new InputStreamReader(
          proc.getErrorStream()));

      BufferedReader pin = new BufferedReader(new InputStreamReader(
          proc.getInputStream()));
      while (pin.readLine() != null)
        ;
      while ((line = lRet.readLine()) != null)
        if (ret != null) ret.append(line);
      proc.waitFor();
      if (logger.isDebugEnabled()) {
        StringBuffer buf = new StringBuffer("");
        buf.append("Eseguita chiamata al compositore: ");
        buf.append(asCmd);
        buf.append(" pars( ");
        buf.append(vPar.toString());
        buf.append("), codice ritorno:");
        buf.append(proc.exitValue());
        if (ret != null) {
          buf.append(", output:");
          buf.append(ret.toString());
        }

        logger.debug(buf.toString());
      }
      return proc.exitValue();
    } catch (IOException e) {
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_IN_ESECUZIONE_COMPOSITORE,
          new String[] {
              ServizioCompositore.getStringJndiEnvironment(CompositoreModelli.PROP_PATH_COMPO),
              e.getMessage() });
    } catch (InterruptedException e) {
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_IN_ESECUZIONE_COMPOSITORE,
          new String[] {
              ServizioCompositore.getStringJndiEnvironment(CompositoreModelli.PROP_PATH_COMPO),
              e.getMessage() });
    }
  }

  /**
   * Esecuzione del compositore da eseguibile esterno P.S. Per i parametri e il
   * tipo di comando vedere l'eseguibile compositore esterno
   *
   * @param pathCompositore path al compositore (eseguibile o batch)
   * @param asCmd
   *        Tipo di comando
   * @param asParams
   *        Parametri per il comando
   * @param ret
   *        Buffer che conterrà l'eventuale errore o il file d'uscita
   * @return
   */
  private int eseguiCompoJava(String pathCompositore, String asParams[], StringBuffer ret)
      throws CompositoreException {
    Vector<String> vPar = new Vector<String>();
    String lsExt = "";
    if (pathCompositore.indexOf('.') > 0)
      lsExt = pathCompositore.substring(pathCompositore.lastIndexOf('.'));
    if (".cmd".equalsIgnoreCase(lsExt) || ".bat".equalsIgnoreCase(lsExt)) {
      // Si tratta di un comando batch windows e va aggiunta l'apertura di una sessione di comandi
      vPar.add("cmd.exe");
      vPar.add("/C");
    }
    vPar.add(pathCompositore);
    for (int i = 0; i < asParams.length; i++) {
      if (asParams[i] != null) vPar.add("\"" + asParams[i] + "\"");
    }

    if (Level.DEBUG_INT == ((AppenderSkeleton)Logger.getRootLogger().getAppender("applicationLog")).getThreshold().toInt()) {
      // se l'appender principale traccia a livello di DEBUG, si aggiunge il parametro per la tracciatura ERRORS, WARNINGS, DEBUG
      vPar.add("ewd");
    }

    String lsParm[] = new String[vPar.size()];
    for (int i = 0; i < vPar.size(); i++)
      lsParm[i] = vPar.get(i);
    try {
      String line;
      String workDir = pathCompositore.substring(0, pathCompositore.lastIndexOf('/'));
      if (logger.isDebugEnabled()) {
        StringBuffer buf = new StringBuffer("");
        buf.append("Sta per essere richiamato il compositore: ");
        buf.append(vPar.toString());
        logger.debug(buf.toString());
      }
      Process proc = Runtime.getRuntime().exec  (lsParm, null, new File(workDir));
      BufferedReader lRet = new BufferedReader(new InputStreamReader(
          proc.getErrorStream()));

      BufferedReader pin = new BufferedReader(new InputStreamReader(
          proc.getInputStream()));
      while (pin.readLine() != null)
        ;
      while ((line = lRet.readLine()) != null)
        if (ret != null) ret.append(line);
      proc.waitFor();
      if (logger.isDebugEnabled()) {
        StringBuffer buf = new StringBuffer("");
        buf.append("Eseguita chiamata al compositore: ");
        buf.append(vPar.toString());
        buf.append(", codice ritorno:");
        buf.append(proc.exitValue());
        buf.append(", output:");
        buf.append(ret.toString());
        logger.debug(buf.toString());
      }
      return proc.exitValue();
    } catch (IOException e) {
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_IN_ESECUZIONE_COMPOSITORE,
          new String[] {
              ServizioCompositore.getStringJndiEnvironment(CompositoreModelli.PROP_PATH_COMPO),
              e.getMessage() });
    } catch (InterruptedException e) {
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_IN_ESECUZIONE_COMPOSITORE,
          new String[] {
              ServizioCompositore.getStringJndiEnvironment(CompositoreModelli.PROP_PATH_COMPO),
              e.getMessage() });
    }
  }

  /**
   * Funzione che esegue la compilazione
   *
   * @param releaseCompositore
   *        Flag che dice se rilasciare il compositore dopo la compilazione
   * @return true se la compilazione viene eseguita correttamente, altrimenti
   *         false
   */
  public boolean compila() throws CompositoreException {
    return this.compila(true);
  }

  /**
   * Funzione che esegue la compilazione
   *
   * @param releaseCompositore
   *        Flag che dice se rilasciare il compositore dopo la compilazione
   * @return true se la compilazione viene eseguita correttamente, altrimenti
   *         false
   */
  private boolean compila(boolean releaseCompositore)
      throws CompositoreException {

    boolean esito = true;
    try {

      if (logger.isDebugEnabled()) logger.debug("compila: inizio metodo");

      boolean continua = true;
      String lsFile = null;
      CompositoreException ce = null;

      if (continua) {
        lsFile = this.getPathModelli() + this.getNomeModello();
        // Verifico l'esistenza del file da compilare
        if (!new File(lsFile).exists()) {
          ce = new CompositoreException(
              CompositoreException.CODICE_ERRORE_MODELLO_NON_TROVATO);
          ce.setParametri(new String[] { this.nomeModello });
          continua = false;
        }
      }

      if (continua) {
        esito = this.compila(lsFile);
        // Se non è andato a buon fine allora leggo il file errori
        if (!esito) {
          String error = null;
          String message = null;
          // Eseguo la lettura del file err
          if (lsFile.indexOf(".") >= 0) {
            String pathErr = lsFile.substring(0, lsFile.lastIndexOf("."))
                + ".err";
            if (logger.isDebugEnabled())
              logger.debug("Leggo l'errore dal file: " + pathErr);
            File fileErr = new File(pathErr);
            if (fileErr.exists()) {
              FileReader fileReader = null;
              try {
                fileReader = new FileReader(pathErr);
                char[] buffer = new char[(int) fileErr.length()];
                fileReader.read(buffer);
                error = CompositoreException.CODICE_ERRORE_COMPILAZIONE;
                message = String.valueOf(buffer).trim();
                fileReader.close();
              } catch (Throwable t) {
                error = "Errore in lettura del file d'errore: "
                    + t.getMessage();
                logger.error(
                    "Errore inaspettato durante la lettura del file degli errori di ritorno dal compositore",
                    t);
              } finally {
                if (fileReader != null) {
                  try {
                    fileReader.close();
                  } catch (Throwable t) {
                  }
                }
                // Elimino il file d'errore
                fileErr.delete();
              }
            } else {
              error = CompositoreException.CODICE_ERRORE_MODELLO_FILE_ERRORI_ASSENTE;
            }
          }

          ce = new CompositoreException(error);
          if (message != null) ce.setParametri(new String[] { message });
          continua = false;
        }
      }

      if (!continua) throw ce;

      if (logger.isDebugEnabled()) logger.debug("compila: fine metodo");

    } finally {
      if (releaseCompositore) this.pool.setCompositore(this);
    }

    return esito;
  }

  /**
   * Funzione che esegue l'eliminazione di un file composto
   *
   * @param asPathFile
   * @return
   */
  public boolean eliminaFileComposto(String asPathFile) {
    boolean esito = false;

    try {
      esito = this.eliminaFile(asPathFile);
    } catch (CompositoreException e) {
    } finally {
      this.pool.setCompositore(this);
    }

    return esito;
  }

  /**
   * @return Returns the connectString.
   */
  public String getConnectString() {
    return connectString;
  }

  /**
   * @param connectString
   *        The connectString to set.
   */
  public void setConnectString(String connectString) {
    this.connectString = connectString;
  }

  /**
   * Funzione che converte una stringa divisa da ; in un array
   *
   * @param stringa
   * @return
   */
  public static String[] stringToArray(String stringa) {
    StringBuffer buffer = new StringBuffer();
    Vector<String> vettore = new Vector<String>();
    int liNumString = 1;

    if (stringa == null) return null;

    // Scorro tutte la stringa
    for (int i = 0; i < stringa.length(); i++) {
      char lcTmp = stringa.charAt(i);
      // Se si incontra l'eliminatore allora aggiungo il catattere
      // successivo
      switch (lcTmp) {
      case '\\':
        i++;
        buffer.append(stringa.charAt(i));
        break;
      case ';':
        // Si è incontrata la fine di una sezione la appendo al vettore
        vettore.add(buffer.toString());
        buffer = new StringBuffer();
        liNumString++;
        break;
      default:
        buffer.append(lcTmp);
      }
    }
    // Aggiungo l'ultimo vettore
    vettore.add(buffer.toString());
    String[] ritorno = new String[liNumString];
    for (int i = 0; i < liNumString; i++) {
      ritorno[i] = vettore.get(i);
    }

    return ritorno;
  }


  /**
   * Converte un valore aggiungendo "\" all'inizio dei caratteri
   * strani ";" e "\"
   *
   * @param valore
   *        valore da convertire
   * @return valore convertito
   */
  private static String aggiungiBackslashPrimaDiPuntoVirgolaEBackslash(
      String val) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < val.length(); i++) {
      char lcTmp = val.charAt(i);
      switch (lcTmp) {
      case ';':
      case '\\':
        buf.append("\\");
        break;
      }
      buf.append(lcTmp);
    }
    return buf.toString();
  }

  /**
   * Esecuzione delle compilazione di un file
   *
   * @param lsFile
   *        File da compilare
   * @return
   * @throws CompositoreException
   */
  private boolean compila(String lsFile)
      throws CompositoreException {
    // per passare i registri si passano 3 parametri con "NON_USATO" in quanto i
    // registri sono sempre l'argomento 8 della chiamata, e non semplicemente
    // l'ultimo argomento della chiamata

    int liRet = 0;
    if (this.isCompositoreJava()) {
      // nuovo compositore java richiamato mediante batch shell

      // per il nuovo compositore non si effettua la compilazione del documento (usata in fase di upload del documento come strumento di
      // verifica sintattica dello stesso)
    } else {
      // vecchio compositore eseguibile
      liRet = eseguiCompo(this.getPathCompositore(), "-c", new String[] { lsFile,
          this.getConnectString(), String.valueOf(this.getTipoDB()), "NON_USATO", "NON_USATO",
          "NON_USATO", this.registri }, null);
    }

    switch (liRet) {
    case -1: // Errore in connesione
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_CONNESSIONE);
    case -2:
      return false;
    }
    return true;
  }

  private String componiFromSQL(String lsFile, String lsOut,
      String sqlDiSelezione) throws CompositoreException {
    StringBuffer ret = new StringBuffer();

    int liRet = 0;
    if (this.isCompositoreJava()) {
      // nuovo compositore java richiamato mediante batch shell: non prevede la composizione a partire da un SQL

      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_MODO_NON_SUPPORTATO);
    } else {
      // vecchio compositore eseguibile
      liRet = eseguiCompo(this.getPathCompositore(), "-o", new String[] { lsFile,
        this.getConnectString(), String.valueOf(this.getTipoDB()), lsOut,
        "!" + sqlDiSelezione, "(null)", this.registri }, ret);
    }
    switch (liRet) {
    case -1: // Errore in connesione
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_CONNESSIONE);
    case -2:
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_MODELLO_NON_COMPILATO);
    case -3:
      return ret.toString() + "!";
    }
    return ret.toString();
  }

  private String componi(String lsFile, String lsOut, String lsPartenza,
      int row, boolean lastRow, boolean append, boolean generaPdf)
      throws CompositoreException {
    StringBuffer ret = new StringBuffer();

    String estensioneModello = lsFile.substring(lsFile.lastIndexOf('.'));
    if (this.isCompositoreJava() && generaPdf) {
      // nel caso di generazione pdf si forza l'estensione a pdf (modalita' valida solo per il compositore java)
      estensioneModello = ".pdf";
    }

    int liRet = 0;
    if (this.isCompositoreJava()) {
      // nuovo compositore java richiamato mediante batch shell: non supporta il passaggio dei dati mediante XML

      if (ENTITA_CHIAVE_FITTIZIA_PER_DATASOURCE_XML.equals(lsPartenza)) {
        // la composizione a partire da un datasource XML non viene piu' supportata con il compositore java
        throw new CompositoreException(
            CompositoreException.CODICE_ERRORE_MODO_NON_SUPPORTATO);
      }

      if (!(lastRow && !append)) {
        // la composizione da una griglia di record non e' piu' supportata, mi aspetto un unico record (quindi primo ed ultimo record per
        // cui lastRow=true ed inoltre non serve effettuare concatenazioni con composizioni precedenti pertanto append=false)
        throw new CompositoreException(
            CompositoreException.CODICE_ERRORE_MODO_NON_SUPPORTATO);
      }

      liRet = eseguiCompoJava(this.getPathCompositore(), new String[] { lsFile,
          this.getDbms(), this.getConnectString(), lsPartenza, lsOut + estensioneModello, this.registri}, ret);
    } else {
      // vecchio compositore eseguibile
      StringBuffer buf = new StringBuffer("");
      buf.append("row=" + row + ";");
      if (!lastRow) buf.append("last=0;");
      else buf.append("last=1;");
      if (append) buf.append("append=1;");

      liRet = eseguiCompo(this.getPathCompositore(), "-o", new String[] { lsFile,
        this.getConnectString(), String.valueOf(this.getTipoDB()), lsOut,
        lsPartenza, buf.toString(), this.registri }, ret);
    }
    switch (liRet) {
    case -1: // Errore in connesione
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_CONNESSIONE);
    case -2:
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_MODELLO_NON_COMPILATO);
    case -3:
      return ret.toString() + "!";
    }

    if (this.isCompositoreJava() || StringUtils.contains(ret.toString(), lsOut)) {
      // creo il nome file composto con estensione del file da comporre
      return lsOut + estensioneModello;
    } else {
      return ret.toString();
    }

  }

//  /**
//   * Verifica se il testo è stato compilato attraverso l'eseguibile esterno
//   *
//   * @param lsFile
//   *        path completo del file da verificare se è compilato
//   * @return true se il testo è stato compilato, false altrimenti
//   * @throws CompositoreException
//   */
//  private boolean isCompiled(String lsFile) throws CompositoreException {
//    if (eseguiCompo("-q", new String[] { lsFile }, null) == 1) return true;
//    return false;
//  }

  /**
   * Elimina un file composto
   *
   * @param asPathFile
   *        path completo del file composto da eliminare
   * @return true se il file è stato eliminato, false altrimenti
   * @throws CompositoreException
   */
  private boolean eliminaFile(String asPathFile) throws CompositoreException {
    boolean esito = true;
    // SS 15/01/2015: si elimina la chiamata al compositore e si effettua la cancellazione direttamente da java
    File fileDel = new File(asPathFile);
    if (fileDel.exists()) {
      if (!fileDel.delete()) {
        logger.error("Impossibile rimuovere il file " + asPathFile);
        esito = false;
      } else {
        logger.debug("Eliminato con successo il file " + asPathFile);
      }
    }
    //if (eseguiCompo("-d", new String[] { asPathFile }, null) == 1) return true;
    return esito;
  }

}
