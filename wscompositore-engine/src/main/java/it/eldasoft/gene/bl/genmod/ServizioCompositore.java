/*
 * Created on 13-ott-2006
 *
 * Copyright (c) Maggioli S.p.A.
 * Tutti i diritti sono riservati.
 *
 * Questo codice sorgente e' materiale confidenziale di proprieta' di Maggioli S.p.A.
 * In quanto tale non puo' essere distribuito liberamente ne' utilizzato a meno di
 * aver prima formalizzato un accordo specifico con Maggioli.
 */
package it.eldasoft.gene.bl.genmod;

import it.eldasoft.utils.sql.comp.SqlComposerException;
import it.eldasoft.utils.sql.comp.SqlManager;
import it.eldasoft.utils.utility.UtilityStringhe;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Classe di facade per la classe che interagisce con il compositore per la
 * compilazione/composizione dei modelli. Contiene al suo interno esclusivamente
 * metodi stateless.
 *
 * @author Stefano.Sabbadin
 */
public class ServizioCompositore {

  /** Logger Log4J di classe. */
  static Logger               logger                   = Logger.getLogger(ServizioCompositore.class);

  /** Template delle environment contenenti i path dei modelli per i diversi applicativi. */
  public static final String  PROP_PATH_MODELLI        = "pathModelli";

  /** Template delle environment in cui sono scritti i parametri di connessione al DB per il compositore. */
  public static final String  ENVIRONMENT_TEMPLATE_PARAMS_CONNESSIONE_DB = "connectStringParam";

  /** Environment contenente il path di output dei modelli composti. */
  public static final String  PROP_PATH_MODELLI_OUTPUT = "pathModelliComposti.relativa";

  /**
   * Separatore da utilizzare nella creazione delle chiavi delle environment da estrarre.
   */
  private static final String SEPARATORE_PATH_CODAPP   = ".";

  /**
   * Property di configurazione che individua il codice univoco attribuito
   * all'applicazione
   */
  public static final String  PROP_DATABASE            = "dbms";

  /** Environment JNDI per recuperare la configurazione con il set di estensioni dei modelli da elaborare con il nuovo compositore Java. */
  private static final String ENVIRONMENT_COMPO_JAVA_INPUT = "input.compo.java";

  /** Environment JNDI per recuperare la configurazione con il path del nuovo compositore Java. */
  private static final String ENVIRONMENT_COMPO_JAVA_PATH = "path.compo.java";

  /** Environment JNDI per recuperare la configurazione con il path del vecchio compositore legacy C. */
  private static final String ENVIRONMENT_COMPO_LEGACY_PATH = "path.compo.legacy";

  /** Wildcard indicante che tutte il compositore elabora tutte le estensioni. */
  private static final String WILDCARD_ESTENSIONI = "*";

  /** Separatore di default tra valori in una environment JNDI di tipo stringa multivalore. */
  private static final String SEPARATORE_MULTIVALORE_DEFAULT = "|";

  /** Separatore tra valori in una environment JNDI di tipo stringa multivalore. */
  private static final String ENVIRONMENT_SEPARATORE_PARAMETRI_MULTIVALORE = "param.separator";

  /**
   * Resource bundle del server compositore
   */
  public ResourceBundle       resBundle                = ResourceBundle.getBundle("CompositoreResources");

  /**
   * Esegue la compilazione del modello mediante la verifica del contenuto
   * dell'RTF e la generazione dei file ausiliari per la composizione successiva
   * del modello stesso
   *
   * @param nomeModello
   *        nome del file del modello
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         compilazione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @deprecated deprecato dalla versione 2.0.0; utilizzare compilaModello
   */
  @Deprecated
  public void compila(String nomeModello, String idApplicazione,
      String codiceApplicazione) throws CompositoreException {
    if (logger.isDebugEnabled()) logger.debug("compila: inizio metodo");
    this.compile(nomeModello, idApplicazione, codiceApplicazione, null, true);
    if (logger.isDebugEnabled()) logger.debug("compila: fine metodo");
  }

  /**
   * Esegue la compilazione del modello mediante la verifica del contenuto
   * dell'RTF e la generazione dei file ausiliari per la composizione successiva
   * del modello stesso; vengono passati eventuali registri da inoltrare al compositore
   *
   * @param nomeModello
   *        nome del file del modello
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @param registri
   *        elenco di coppie di registri nel formato NUMERO=VALORE separate da
   *        ";". Attualmente i registri utilizzati sono:
   *        <ul>
   *        <li>80: id di sessione per i parametri</li>
   *        <li>81: id contesto oracle</li>
   *        <li>86: id utente</li>
   *        <li>87: "pwb"</li>
   *        </ul>
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         compilazione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @since 2.0.0
   */
  public void compilaModello(String nomeModello, String idApplicazione,
      String codiceApplicazione, String registri) throws CompositoreException {
    if (logger.isDebugEnabled()) logger.debug("compilaModello: inizio metodo");
    this.compile(nomeModello, idApplicazione, codiceApplicazione, registri, true);
    if (logger.isDebugEnabled()) logger.debug("compilaModello: fine metodo");
  }

  /**
   * Esegue la compilazione del modello mediante la verifica del contenuto
   * dell'RTF e la generazione dei file ausiliari per la composizione successiva
   * del modello stesso senza utilizzare la connessione al DB per verificare i
   * mnemonici indicati in quanto si presuppone di utilizzare questa modalità di
   * compilazione esclusivamente per i modelli da comporre a partire dai dati
   * inviati mediante file XML; vengono passati eventuali registri da inoltrare
   * al compositore
   *
   * @param nomeModello
   *        nome del file del modello
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @param registri
   *        elenco di coppie di registri nel formato NUMERO=VALORE separate da
   *        ";". Attualmente i registri utilizzati sono:
   *        <ul>
   *        <li>80: id di sessione per i parametri</li>
   *        <li>81: id contesto oracle</li>
   *        <li>86: id utente</li>
   *        <li>87: "pwb"</li>
   *        </ul>
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         compilazione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @since 2.1.0
   */
  public void compilaModelloSenzaConnessioneDB(String nomeModello, String idApplicazione,
      String codiceApplicazione, String registri) throws CompositoreException {
    if (logger.isDebugEnabled()) logger.debug("compilaModelloSenzaConnessioneDB: inizio metodo");
    this.compile(nomeModello, idApplicazione, codiceApplicazione, registri, false);
    if (logger.isDebugEnabled()) logger.debug("compilaModelloSenzaConnessioneDB: fine metodo");
  }

  /**
   * Esegue la compilazione del modello mediante la verifica del contenuto
   * dell'RTF e la generazione dei file ausiliari per la composizione successiva
   * del modello stesso
   *
   * @param nomeModello
   *        nome del file del modello
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @param registri
   *        elenco di coppie di registri nel formato NUMERO=VALORE separate da
   *        ";". Attualmente i registri utilizzati sono:
   *        <ul>
   *        <li>80: id di sessione per i parametri</li>
   *        <li>81: id contesto oracle</li>
   *        <li>86: id utente</li>
   *        <li>87: "pwb"</li>
   *        </ul>
   * @param connettiDB
   *        true se ci si deve connettere al db indicato nella configurazione,
   *        false altrimenti
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         compilazione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   */
  private void compile(String nomeModello, String idApplicazione,
      String codiceApplicazione, String registri, boolean connettiDB) throws CompositoreException {
    CompositoreModelli compilatore = null;
    try {
      String pathModelli = ServizioCompositore.getPathModelli(idApplicazione,
          codiceApplicazione);

      boolean isCompositoreJava = ServizioCompositore.isForCompositoreJava(nomeModello);

      String connectionString = "NOT_FOUND";
      // si legge la tipologia di DBMS associata all'applicativo, e si estrae il
      // primo carattere che sarà "*" nel caso eccezionale che sia stato
      // configurato male lo stesso o "N" nel caso di compilazione di un modello
      // indipendente dalla base dati
      String dbms = null;
      char tipoDB = 'X';
      if (connettiDB) {
        //connectionString = ServizioCompositore.getConnectionString(idApplicazione);
        try {
          dbms = getStringJndiEnvironment(ServizioCompositore.PROP_DATABASE
              + SEPARATORE_PATH_CODAPP
              + idApplicazione);
          tipoDB = SqlManager.getCodiceDatabasePerCompositore(dbms);
        } catch (SqlComposerException e) {
          logger.error(this.resBundle.getString(e.getChiaveResourceBundle()), e);
          CompositoreException ce = new CompositoreException(
              CompositoreException.CODICE_ERRORE_CONNECTION_DB_NON_VALORIZZATA);
          ce.setParametri(new String[] { idApplicazione });
          throw ce;
        }
        connectionString = ServizioCompositore.getConnectionString(idApplicazione, isCompositoreJava, tipoDB);
      } else {
        dbms = "N";
        tipoDB = 'N';
        connectionString = "NODB";
      }

      // Setto tutti i parametri del compositore
      compilatore = PoolCompositoreModelli.getInstance().getCompositore(isCompositoreJava);
      compilatore.setPathModelli(pathModelli);
      compilatore.setNomeModello(nomeModello);
      compilatore.setConnectString(connectionString);
      // fix per il nuovo compositore java: eventuali ";" alla fine della lista dei registri causa un errore
      compilatore.setRegistri(registri);
      compilatore.setDbms(dbms);
      compilatore.setTipoDB(tipoDB);
      if (logger.isDebugEnabled()) {
        logger.debug("Path modelli: "
            + compilatore.getPathModelli()
            + "\nNome file: "
            + nomeModello
            + "\nTipo DB: "
            + compilatore.getTipoDB());
      }
      compilatore.compila();

    } catch (CompositoreException e) {
      logger.error(UtilityStringhe.replaceParametriMessageBundle(
          this.resBundle.getString(e.getChiaveResourceBundle()),
          e.getParametri()), e);
      throw e;
    } catch (Throwable t) {
      CompositoreException ce = new CompositoreException(
          CompositoreException.CODICE_ERRORE_INASPETTATO_COMPILAZIONE);
      logger.error(this.resBundle.getString(ce.getChiaveResourceBundle()), t);
      throw ce;
    }
  }

  /**
   * Funzione che esegue la composizione di un modello senza utilizzare
   * parametri da inviare al compositore
   *
   * @param nomeModello
   *        nome del file del modello
   * @param entita
   *        entità di partenza
   * @param elencoChiavi
   *        elenco dei campi chiave, separati da ";"
   * @param valoriChiavi
   *        per ogni record da elaborare è presente un elemento contenente i
   *        valori delle chiavi separate da ";"
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @return Ritorna il path completo del file compilato
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         composizione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @deprecated deprecato dalla versione 2.0.0; utilizzare componi
   */
  @Deprecated
  public String componiModello(String nomeModello, String entita,
      String elencoChiavi, String[] valoriChiavi, String idApplicazione,
      String codiceApplicazione) throws CompositoreException {

    if (logger.isDebugEnabled()) logger.debug("componiModello: inizio metodo");

    String fileComposto = this.compose(nomeModello, entita, elencoChiavi,
        valoriChiavi, null, idApplicazione, codiceApplicazione, null, false);

    if (logger.isDebugEnabled()) logger.debug("componiModello: fine metodo");
    return fileComposto;
  }

  /**
   * Funzione che esegue la composizione di un modello utilizzando dei parametri
   * da inviare al compositore
   *
   * @param nomeModello
   *        nome del file del modello
   * @param entita
   *        entità di partenza
   * @param elencoChiavi
   *        elenco dei campi chiave, separati da ";"
   * @param valoriChiavi
   *        per ogni record da elaborare è presente un elemento contenente i
   *        valori delle chiavi separate da ";"
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @param idSessione
   *        identificativo a cui sono collegati i parametri
   * @return Ritorna il path completo del file compilato
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         composizione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @deprecated deprecato dalla versione 2.0.0; utilizzare componi
   */
  @Deprecated
  public String componiModelloConParametri(String nomeModello, String entita,
      String elencoChiavi, String[] valoriChiavi, String idApplicazione,
      String codiceApplicazione, int idSessione) throws CompositoreException {

    if (logger.isDebugEnabled())
      logger.debug("componiModelloConParametri: inizio metodo");

    String fileComposto = this.compose(nomeModello, entita, elencoChiavi,
        valoriChiavi, null, idApplicazione, codiceApplicazione, "80="+idSessione, false);

    if (logger.isDebugEnabled())
      logger.debug("componiModelloConParametri: fine metodo");
    return fileComposto;
  }

  /**
   * Funzione che esegue la composizione di un modello senza utilizzare
   * parametri da inviare al compositore
   *
   * @param nomeModello
   *        nome del file del modello
   * @param entita
   *        entità di partenza
   * @param elencoChiavi
   *        elenco dei campi chiave, separati da ";"
   * @param valoriChiavi
   *        per ogni record da elaborare è presente un elemento contenente i
   *        valori delle chiavi separate da ";"
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @param registri
   *        elenco di coppie di registri nel formato NUMERO=VALORE separate da
   *        ";". Attualmente i registri utilizzati sono:
   *        <ul>
   *        <li>80: id di sessione per i parametri</li>
   *        <li>81: id contesto oracle</li>
   *        <li>86: id utente</li>
   *        <li>87: "pwb"</li>
   *        </ul>
   * @return Ritorna il path completo del file compilato
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         composizione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @since 2.0.0
   */
  public String componi(String nomeModello, String entita,
      String elencoChiavi, String[] valoriChiavi, String idApplicazione,
      String codiceApplicazione, String registri, Boolean generaPdf) throws CompositoreException {

    if (logger.isDebugEnabled()) {
      logger.debug("componi("
          + nomeModello
          + ", "
          + entita
          + ", "
          + elencoChiavi
          + ", ["
          + StringUtils.join(valoriChiavi)
          + "], "
          + idApplicazione
          + ", "
          + codiceApplicazione
          + ", "
          + registri
          + ", "
          + generaPdf
          + "): inizio metodo");
    }

    if (generaPdf == null) {
      // retrocompatibilita': nel caso di interfacciamento da parte di webapp con geneweb non allineato, si garantisce la generazione
      // dell'output nello stesso formato del modello
      generaPdf = false;
    }

    String fileComposto = this.compose(nomeModello, entita, elencoChiavi,
        valoriChiavi, null, idApplicazione, codiceApplicazione, registri, generaPdf);

    if (logger.isDebugEnabled()) logger.debug("componi: fine metodo");
    return fileComposto;
  }

  /**
   * Funzione che esegue la composizione di un modello dato in input e non presente in area shared. <br/>
   * <b>NB: funziona esclusivamente su modelli che non includono ulteriori sottomodelli.</b>
   *
   * @param nomeModello
   *        nome del file del modello
   * @param contenutoModello
   *        contenuto binario del modello da comporrre
   * @param entita
   *        entità di partenza
   * @param elencoChiavi
   *        elenco dei campi chiave, separati da ";"
   * @param valoriChiavi
   *        per ogni record da elaborare è presente un elemento contenente i valori delle chiavi separate da ";"
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione richiedente
   * @param registri
   *        elenco di coppie di registri nel formato NUMERO=VALORE separate da ";". Attualmente i registri utilizzati sono:
   *        <ul>
   *        <li>80: id di sessione per i parametri</li>
   *        <li>81: id contesto oracle</li>
   *        <li>86: id utente</li>
   *        <li>87: "pwb"</li>
   *        </ul>
   * @return Ritorna il contenuto del modello composto
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua composizione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @since 2.2.0
   */
  public byte[] componiStream(String nomeModello, byte[] contenutoModello, String entita,
      String elencoChiavi, String[] valoriChiavi, String idApplicazione,
      String codiceApplicazione, String registri, Boolean generaPdf) throws CompositoreException {

    if (logger.isDebugEnabled()) {
      logger.debug("componiStream("
          + nomeModello
          + ", byte[], "
          + entita
          + ", "
          + elencoChiavi
          + ", ["
          + StringUtils.join(valoriChiavi)
          + "], "
          + idApplicazione
          + ", "
          + codiceApplicazione
          + ", "
          + registri
          + ", "
          + generaPdf
          + "): inizio metodo");
    }

    if (generaPdf == null) {
      // retrocompatibilita': nel caso di interfacciamento da parte di webapp con geneweb non allineato, si garantisce la generazione
      // dell'output nello stesso formato del modello
      generaPdf = false;
    }

    String pathModelli = ServizioCompositore.getPathModelli(idApplicazione,
        codiceApplicazione);

    String nomeModelloTemporaneo = UUID.randomUUID().toString()
        + "_"
        + nomeModello;

    File fileModello = null;
    FileOutputStream fos = null;
    String nomefileComposto = null;
    File fileComposto = null;
    byte[] contenutoFileComposto = null;
    try {
      // si crea il file di modello nell'area shared relativa all'applicativo individuato da idApplicazione e codiceApplicazione
      fileModello = new File(pathModelli + nomeModelloTemporaneo);
      fos = new FileOutputStream(fileModello);
      fos.write(contenutoModello);
      fos.flush();
      fos.close();
      // si compone il modello
      nomefileComposto = this.compose(nomeModelloTemporaneo, entita, elencoChiavi, valoriChiavi, null, idApplicazione, codiceApplicazione,
          registri, generaPdf);
      if (nomefileComposto != null) {
        // va letto il contenuto del file
        fileComposto = new File(pathModelli + getStringJndiEnvironment(ServizioCompositore.PROP_PATH_MODELLI_OUTPUT) + nomefileComposto);
        contenutoFileComposto = FileUtils.readFileToByteArray(fileComposto);
      }

    } catch (Exception e) {
      if (fos == null) {
        CompositoreException ce = new CompositoreException(CompositoreException.CODICE_ERRORE_MODELLO_NON_TROVATO);
        ce.setParametri(new String[] {nomeModelloTemporaneo });
        throw ce;
      }
    } finally {
      // si rimuove il file composto
      if (fileComposto != null) {
        fileComposto.delete();
      }
      // si rimuovono i file di supporto compilati a partire dal modello
      if (fos != null) {
        String nomeFileSenzaEstensione = nomeModelloTemporaneo.substring(0, nomeModelloTemporaneo.lastIndexOf("."));
        File dir = new File(pathModelli);
        FileFilter fileFilter = new WildcardFileFilter(nomeFileSenzaEstensione + ".*");
        File[] files = dir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
           files[i].delete();
        }
      }
    }

    if (logger.isDebugEnabled()) logger.debug("componiStream: fine metodo");
    return contenutoFileComposto;
  }

  /**
   * Funzione che esegue la composizione di un modello senza utilizzare
   * parametri da inviare al compositore
   *
   * @param nomeModello
   *        nome del file del modello
   * @param nomeFileSorgenteDati
   *        nome del file in "Modelli/out" contenente i dati da utilizzare, in
   *        formato XML, per la composizione del report
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @param registri
   *        elenco di coppie di registri nel formato NUMERO=VALORE separate da
   *        ";". Attualmente i registri utilizzati sono:
   *        <ul>
   *        <li>80: id di sessione per i parametri</li>
   *        <li>81: id contesto oracle</li>
   *        <li>86: id utente</li>
   *        <li>87: "pwb"</li>
   *        </ul>
   * @return Ritorna il path completo del file compilato
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua
   *         composizione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   *
   * @since 2.1.0
   */
  public String componiModelloSenzaConnessioneDB(String nomeModello,
      String nomeFileSorgenteDati, String idApplicazione,
      String codiceApplicazione, String registri) throws CompositoreException {

    if (logger.isDebugEnabled()) logger.debug("componiModelloSenzaConnessioneDB: inizio metodo");

    String fileComposto = this.compose(nomeModello, null, null,
        null, nomeFileSorgenteDati, idApplicazione, codiceApplicazione, registri, false);

    if (logger.isDebugEnabled()) logger.debug("componiModelloSenzaConnessioneDB: fine metodo");
    return fileComposto;
  }

  /**
   * Centralizza la composizione del modello
   *
   * @param nomeModello
   *        nome del file del modello
   * @param entita
   *        entità di partenza
   * @param elencoChiavi
   *        elenco dei campi chiave, separati da ";"
   * @param valoriChiavi
   *        per ogni record da elaborare è presente un elemento contenente i valori delle chiavi separate da ";"
   * @param nomeFileSorgenteDati
   *        nome del file in "Modelli/out" contenente i dati da utilizzare, in formato XML, per la composizione del report
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione richiedente
   * @param registri
   *        registri da inoltrare al compositore
   * @param generaPdf
   *        false se si vuole generare il documento con la stessa estensione del modello in input, true se si vuole ritornare un pdf
   * @return Ritorna il path completo del file compilato
   * @throws CompositoreException
   *         eccezione dovuta a errori in fase di apertura file, o sua composizione, o errori dovuti a problemi di interfacciamento con il
   *         motore nella DLL
   */
  private String compose(String nomeModello, String entita,
      String elencoChiavi, String[] valoriChiavi, String nomeFileSorgenteDati,
      String idApplicazione, String codiceApplicazione, String registri, boolean generaPdf)
      throws CompositoreException {
    String fileComposto = null;
    CompositoreModelli compositore = null;

    try {
      String pathModelli = ServizioCompositore.getPathModelli(idApplicazione,
          codiceApplicazione);

      File fileModello = new File(pathModelli + nomeModello);

      if (!fileModello.exists()) {
        CompositoreException ce = new CompositoreException(CompositoreException.CODICE_ERRORE_MODELLO_NON_TROVATO);
        ce.setParametri(new String[] {nomeModello });
        throw ce;
      }

      boolean isCompositoreJava = ServizioCompositore.isForCompositoreJava(nomeModello);

      String connectionString = "NOT_FOUND";
      // si legge la tipologia di DBMS associata all'applicativo, e si estrae il
      // primo carattere che sarà "*" nel caso eccezionale che sia stato
      // configurato male lo stesso o "N" nel caso di compilazione di un modello
      // indipendente dalla base dati
      String dbms = null;
      char tipoDB = 'X';
      if (nomeFileSorgenteDati == null) {
        try {
          dbms = getStringJndiEnvironment(ServizioCompositore.PROP_DATABASE
              + SEPARATORE_PATH_CODAPP
              + idApplicazione);
          tipoDB = SqlManager.getCodiceDatabasePerCompositore(dbms);
        } catch (SqlComposerException e) {
          logger.error(this.resBundle.getString(e.getChiaveResourceBundle()), e);
          CompositoreException ce = new CompositoreException(
              CompositoreException.CODICE_ERRORE_CONNECTION_DB_NON_VALORIZZATA);
          ce.setParametri(new String[] { idApplicazione });
          throw ce;
        }
        connectionString = ServizioCompositore.getConnectionString(idApplicazione, isCompositoreJava, tipoDB);
      } else {
        dbms = "N";
        tipoDB = 'N';
        connectionString = "NODB";
      }

      // Setto tutti i parametri del compositore
      compositore = PoolCompositoreModelli.getInstance().getCompositore(isCompositoreJava);
      compositore.setPathModelli(pathModelli);
      compositore.setPathOutput(pathModelli
          + getStringJndiEnvironment(ServizioCompositore.PROP_PATH_MODELLI_OUTPUT));
      compositore.setNomeModello(nomeModello);
      compositore.setConnectString(connectionString);
      compositore.setNomeFileSorgenteDati(nomeFileSorgenteDati);
      compositore.setDbms(dbms);
      compositore.setTipoDB(tipoDB);
      compositore.setEntita(entita);
      compositore.setCampiChiave(elencoChiavi);
      // fix per il nuovo compositore java: eventuali ";" alla fine della lista dei registri causa un errore
      compositore.setRegistri(registri);

      if (logger.isDebugEnabled()) {
        logger.debug("Path Modelli: "
            + compositore.getPathModelli()
            + "\nNome File: "
            + nomeModello
            + "\nConnectString: "
            + compositore.getConnectString()
            + "\nTipo DB: "
            + compositore.getTipoDB());
      }

      if (nomeFileSorgenteDati == null) {
        for (int i = 0; i < valoriChiavi.length; i++) {
          compositore.setValoriChiave(valoriChiavi[i]);
          // viene aggiunto l'attributo "idSessione" al CompositoreModelli e non
          // differenziata la chiamata in componi e componiConParametri perchè
          // il codice della componi è piuttosto lungo e complesso
          compositore.componi(i, i == (valoriChiavi.length - 1), generaPdf);
        }
      } else {
        // nel caso di composizione a partire da una sorgente dati su file si
        // esegue un'unica composizione
        compositore.componi(0, true, generaPdf);
      }

      // Elimino il path dal nome del file composto
      fileComposto = compositore.getFileComposto();
      if (fileComposto.indexOf("/") >= 0) {
        fileComposto = fileComposto.substring(fileComposto.lastIndexOf("/") + 1);
      }

    } catch (CompositoreException e) {
      logger.error(UtilityStringhe.replaceParametriMessageBundle(
          this.resBundle.getString(e.getChiaveResourceBundle()),
          e.getParametri()), e);
      throw e;
    } catch (Throwable t) {
      CompositoreException ce = new CompositoreException(
          CompositoreException.CODICE_ERRORE_INASPETTATO_COMPOSIZIONE);
      logger.error(this.resBundle.getString(ce.getChiaveResourceBundle()), t);
      throw ce;
    }
    return fileComposto;
  }

  /**
   * Elimina il file prodotto da una precedente composizione.
   *
   * @param nomeFileComposto
   *        nome del file da eliminare
   * @param idApplicazione
   *        identificativo dell'applicazione richiedente
   * @param codiceApplicazione
   *        identificativo del codice applicativo utilizzato nell'applicazione
   *        richiedente
   * @throws CompositoreException
   */
  public void eliminaFileComposto(String nomeFileComposto,
      String idApplicazione, String codiceApplicazione)
      throws CompositoreException {
    if (logger.isDebugEnabled())
      logger.debug("eliminaFileComposto: inizio metodo");

    CompositoreModelli compositore = null;

    try {
      String pathModelli = ServizioCompositore.getPathModelli(idApplicazione,
          codiceApplicazione);

      // questa operazione effettua la rimozione del file direttamente senza richiamare il compositore, pertanto il valore del parametro in
      // input e' indifferente
      compositore = PoolCompositoreModelli.getInstance().getCompositore(false);
      compositore.eliminaFileComposto(pathModelli
          + getStringJndiEnvironment(ServizioCompositore.PROP_PATH_MODELLI_OUTPUT)
          + nomeFileComposto);
    } catch (CompositoreException e) {
      logger.error(UtilityStringhe.replaceParametriMessageBundle(
          this.resBundle.getString(e.getChiaveResourceBundle()),
          e.getParametri()), e);
      throw e;
    }
    if (logger.isDebugEnabled())
      logger.debug("eliminaFileComposto: fine metodo");
  }

  /**
   * Ritorna le estensioni indicate nella environment JNDI "input.compo.java".
   *
   * @return "*" per tutte le estensioni, altrimenti il set di estensioni.
   * @throws CompositoreException
   */
  public String[] getEstensioniModelloOutputPDF() {
    if (logger.isDebugEnabled()) logger.debug("getEstensioniModelloOutputPDF: inizio metodo");

    String[] estensioni = null;

    // verifica se l'estensione del modello rientra tra quelli gestiti dal compositore java
    String environment = ServizioCompositore.ENVIRONMENT_COMPO_JAVA_INPUT;
    String estensioniAmmesse = StringUtils.stripToNull(ServizioCompositore.getStringJndiEnvironment(environment));
    String paramSeparator = ServizioCompositore.getParamSeparator();
    if (estensioniAmmesse != null) {
      estensioni = StringUtils.split(estensioniAmmesse.toUpperCase(), paramSeparator);
    }

    if (logger.isDebugEnabled()) logger.debug("getEstensioniModelloOutputPDF: fine metodo");

    return estensioni;
  }


  /**
   * Legge dalle properties il path alla cartella dei modelli per l'applicazione
   * e il sottoapplicativo in input e ne verifica l'esistenza
   *
   * @param idApplicazione
   *        identificativo univoco di applicazione
   * @param codiceApplicazione
   *        codice applicativo
   * @return path alla cartella dei modelli
   * @throws CompositoreException
   *         eccezione emessa quando il path non è valorizzato o punta ad una
   *         cartella inesistente
   */
  private static String getPathModelli(String idApplicazione,
      String codiceApplicazione) throws CompositoreException {

    String pathModelli = getStringJndiEnvironment(ServizioCompositore.PROP_PATH_MODELLI
        + SEPARATORE_PATH_CODAPP
        + idApplicazione
        + SEPARATORE_PATH_CODAPP
        + codiceApplicazione);
    // check esistenza property configurazione
    if (pathModelli == null) {
      CompositoreException ce = new CompositoreException(
          CompositoreException.CODICE_ERRORE_PATH_NON_VALORIZZATO);
      ce.setParametri(new String[] { idApplicazione, codiceApplicazione });
      throw ce;
    }
    // check esistenza cartella
    File f = new File(pathModelli);
    if (!f.exists()) {
      CompositoreException ce = new CompositoreException(
          CompositoreException.CODICE_ERRORE_PATH_NON_TROVATO);
      ce.setParametri(new String[] { idApplicazione, codiceApplicazione });
      throw ce;
    }

    return pathModelli;
  }

  /**
   * Dato il nome del file del modello in input, viene analizzata l'estensione del file per comprendere se il modello verr&agrave; elaborato
   * dal compositore Java oppure dal compositore Legacy scritto in C.
   *
   * @param nomeModello
   *        modello da analizzare
   * @return true se il modello ha una delle estensioni previste da configurazione per cui va elaborato dal compositore Java, false
   *         altrimenti (in tal caso verr&agrave; preso in carico dal compositore Legacy)
   */
  private static boolean isForCompositoreJava(String nomeModello) {
    boolean isForJava = false;
    // estensione del file da elaborare
    String estensione = nomeModello.toUpperCase().substring(nomeModello.lastIndexOf('.')+1);
    // verifica se l'estensione del modello rientra tra quelli gestiti dal compositore java
    String environment = ServizioCompositore.ENVIRONMENT_COMPO_JAVA_INPUT;
    String estensioniAmmesse = StringUtils.stripToNull(ServizioCompositore.getStringJndiEnvironment(environment));
    if (ServizioCompositore.WILDCARD_ESTENSIONI.equals(estensioniAmmesse)) {
      isForJava = true;
    } else {
      if (estensioniAmmesse != null) {
        // si effettua lo split
        String paramSeparator = ServizioCompositore.getParamSeparator();
        List<String> listaEstensioniAmmesse = Arrays.asList(estensioniAmmesse.toUpperCase().split("\\"+paramSeparator));
        isForJava = listaEstensioniAmmesse.contains(estensione);
      }
    }
    return isForJava;
  }

  /**
   * Estrae della configurazione il separatore indicato nella environment del file di contesto. Se fosse stato erroneamente sbiancato si
   * utilizza il valore di default.
   *
   * @return carattere separatore utilizzato nelle configurazioni di environment multivalore
   */
  private static String getParamSeparator() {
    String paramSeparator = StringUtils.stripToNull(ServizioCompositore.getStringJndiEnvironment(ServizioCompositore.ENVIRONMENT_SEPARATORE_PARAMETRI_MULTIVALORE));
    if (paramSeparator == null) {
      // si ripristina valore di default, quello inserito a standard nel file di contesto, ovvero il "|"
      paramSeparator = ServizioCompositore.SEPARATORE_MULTIVALORE_DEFAULT;
    }
    return paramSeparator;
  }

  /**
   * Ritorna il percorso del compositore da porre in esecuzione.
   *
   * @param isCompositoreJava
   *        true se va ritornato il compositore Java, false se va ritornato il compositore legacy C
   *
   * @return path del compositore eseguibile da eseguire per completare l'elaborazione
   */
  protected static String getPathCompositore(boolean isCompositoreJava) {
    String path = null;
    String environment = ServizioCompositore.ENVIRONMENT_COMPO_LEGACY_PATH;
    if (isCompositoreJava) {
      environment = ServizioCompositore.ENVIRONMENT_COMPO_JAVA_PATH;
    }
    path = ServizioCompositore.getStringJndiEnvironment(environment);
    return path;
  }

  /**
   * Crea la connection string per l'applicazione sulla base del db e del compositore da utilizzare
   *
   * @param idApplicazione
   *        identificativo univoco dell'applicazione
   * @param isCompositoreJava
   *        true se deve andare in esecuzione il compositore java, false altrimenti (compo legacy)
   * @param tipoDBMS
   *        'O' per Oracle, 'M' per SQL Server, 'P' per PostgreSQL
   * @return connection string definita per connettersi al db con il compositore individuato in input
   * @throws CompositoreException
   */
  private static String getConnectionString(String idApplicazione, boolean isCompositoreJava, char tipoDBMS) throws CompositoreException {
    StringBuilder environment = new StringBuilder(ServizioCompositore.ENVIRONMENT_TEMPLATE_PARAMS_CONNESSIONE_DB);
    environment.append(ServizioCompositore.SEPARATORE_PATH_CODAPP).append(idApplicazione).append(ServizioCompositore.SEPARATORE_PATH_CODAPP);
    // di default il formato dei parametri di connessione e' per la connection string JDBC
    String format = "JDBC";
    boolean isOdbc = false;
    if (!isCompositoreJava) {
      // nel caso di compositore legacy va verificato se si utilizza il compo basato su odbc o su jdbc
      String pathCompositore = getPathCompositore(isCompositoreJava);
      if (StringUtils.containsIgnoreCase(pathCompositore, "compo_odbc")) {
        isOdbc = true;
        format = "DSN";
      }
    }
    environment.append(format);

    // a questo punto si leggono i parametri
    String connectStringParam = StringUtils.stripToNull(ServizioCompositore.getStringJndiEnvironment(environment.toString()));
    if (connectStringParam == null) {
      CompositoreException ce = new CompositoreException(
          CompositoreException.CODICE_ERRORE_CONNECTION_DB_NON_VALORIZZATA);
      ce.setParametri(new String[] { idApplicazione + ServizioCompositore.SEPARATORE_PATH_CODAPP + format });
      throw ce;
    }

    String paramSeparator = ServizioCompositore.getParamSeparator();
    String[] dbParams = StringUtils.split(connectStringParam, paramSeparator);

    StringBuilder connectionString = new StringBuilder();
    if (dbParams.length == 3) {
      // si costruisce, a seconda della casistica legata al compositore da usare ed al DBMS, la stringa di connessione
      if (isCompositoreJava) {
        connectionString.append("DBMS=JDBC;driverClassName=");
        switch (tipoDBMS) {
        case SqlManager.DATABASE_ORACLE_PER_COMPOSITORE:
          connectionString.append("oracle.jdbc.driver.OracleDriver");
          connectionString.append(";url=");
          connectionString.append(dbParams[0]);
          break;
        case SqlManager.DATABASE_SQL_SERVER_PER_COMPOSITORE:
          connectionString.append("com.microsoft.sqlserver.jdbc.SQLServerDriver");
          connectionString.append(";url=");
          connectionString.append(dbParams[0]);
          connectionString.append(";SelectMethod=cursor");
          break;
        case SqlManager.DATABASE_POSTGRES_PER_COMPOSITORE:
          connectionString.append("org.postgresql.Driver");
          connectionString.append(";url=");
          connectionString.append(dbParams[0]);
          break;
        }
        connectionString.append(";UID=");
        connectionString.append(dbParams[1]);
        connectionString.append(";PWD=");
        connectionString.append(dbParams[2]);
      } else {
        // compositore legacy
        if (isOdbc) {
          // compositore legacy odbc
          switch (tipoDBMS) {
          case SqlManager.DATABASE_ORACLE_PER_COMPOSITORE:
            connectionString.append("DRIVER=Microsoft ODBC for Oracle;SERVER=");
            connectionString.append(dbParams[0]);
            break;
          case SqlManager.DATABASE_SQL_SERVER_PER_COMPOSITORE:
          case SqlManager.DATABASE_POSTGRES_PER_COMPOSITORE:
            connectionString.append("DSN=");
            connectionString.append(dbParams[0]);
            break;
          }
        } else {
          // compositore legacy jdbc
          connectionString.append("DBMS=JDBC;driverClassName=");
          switch (tipoDBMS) {
          case SqlManager.DATABASE_ORACLE_PER_COMPOSITORE:
            connectionString.append("oracle.jdbc.driver.OracleDriver");
            connectionString.append(";url=");
            connectionString.append(dbParams[0]);
            break;
          case SqlManager.DATABASE_SQL_SERVER_PER_COMPOSITORE:
            connectionString.append("net.sourceforge.jtds.jdbc.Driver");
            connectionString.append(";url=jdbc:jtds:");
            // per Sql Server la url ha un ":jtds" in piu', pertanto lo si aggiunge applicativamente
            connectionString.append(dbParams[0].substring(dbParams[0].indexOf("jdbc:") + "jdbc:".length()));
            connectionString.append(";charset=iso-8859-1");
            break;
          case SqlManager.DATABASE_POSTGRES_PER_COMPOSITORE:
            connectionString.append("org.postgresql.Driver");
            connectionString.append(";url=");
            connectionString.append(dbParams[0]);
            break;
          }
        }
        connectionString.append(";UID=");
        connectionString.append(dbParams[1]);
        connectionString.append(";PWD=");
        connectionString.append(dbParams[2]);
      }
    }
    return connectionString.toString();
  }

  /**
   * Estrae una configurazione JNDI definita in formato stringa.
   *
   * @param environmentName nome della configurazione
   * @return valore della configurazione, null se non valorizzata
   */
  static String getStringJndiEnvironment(String environmentName) {
    String obj = null;
    try {
      obj = (String)getJndiEnvironment(environmentName);
    } catch (ClassCastException e) {
      logger.error("Impossibile convertire a stringa la configurazione " + environmentName + " dal file di contesto", e);
    }
    return obj;
  }

  /**
   * Estrae una configurazione JNDI definita in formato intero.
   *
   * @param environmentName nome della configurazione
   * @return valore della configurazione, null se non valorizzata
   */
  static Integer getIntegerJndiEnvironment(String environmentName) {
    Integer obj = null;
    try {
      obj = (Integer)getJndiEnvironment(environmentName);
    } catch (ClassCastException e) {
      logger.error("Impossibile convertire a intero la configurazione " + environmentName + " dal file di contesto", e);
    }
    return obj;
  }

  /**
   * Estrae una configurazione JNDI.
   *
   * @param environmentName nome della configurazione
   * @return valore della configurazione, null se non valorizzata
   */
  private static Object getJndiEnvironment(String environmentName) {
    Object obj = null;
    try {
      InitialContext ctx = new InitialContext();
      obj = ctx.lookup("java:comp/env/" + environmentName);
    } catch (NamingException e) {
      logger.error("Impossibile estrarre la configurazione " + environmentName + " dal file di contesto", e);
    }
    return obj;
  }

}
