/*
 * Created on 11-dic-2006
 *
 * Copyright (c) Maggioli S.p.A.
 * Tutti i diritti sono riservati.
 *
 * Questo codice sorgente e' materiale confidenziale di proprieta' di Maggioli S.p.A.
 * In quanto tale non puo' essere distribuito liberamente ne' utilizzato a meno di
 * aver prima formalizzato un accordo specifico con Maggioli.
 */
package it.eldasoft.gene.bl.genmod;

import java.util.ArrayList;
import java.util.List;

/**
 * Pool di compositori, istanziato sulla base di una property di configurazione
 * che indica quanti compositori compongono il pool.<br/>
 * Ogni volta che si necessita di un compositore occorre interrogare questo
 * Singleton per ottenere il primo compositore disponibile (se esiste). Inoltre,
 * ogni volta che un compositore termina le proprie attività, deve ritornare
 * disponibile nel pool.
 *
 * @author Stefano.Sabbadin
 */
public class PoolCompositoreModelli {

  /** Environment JNDI che individua la dimensione del pool di compositori. */
  private static final String           ENVIRONMENT_SIZE_POOLCOMPOSITORI = "poolCompositori.dimensione";

  /** Singleton. */
  private static PoolCompositoreModelli instance;

  /** Pool di compositori. */
  private List<CompositoreModelli>    compositori;

  /**
   * Ritorna l'unica istanza del singleton.
   */
  public static PoolCompositoreModelli getInstance() {
    if (instance != null) return instance;

    synchronized (PoolCompositoreModelli.class) {
      if (instance == null) {
        instance = new PoolCompositoreModelli();
      }
    }
    return instance;
  }

  /**
   * Costruttore privato del singleton: alloca i compositori.
   */
  private PoolCompositoreModelli() {
    this.compositori = new ArrayList<CompositoreModelli>();

    Integer dimensionePool = ServizioCompositore.getIntegerJndiEnvironment(PoolCompositoreModelli.ENVIRONMENT_SIZE_POOLCOMPOSITORI);
    for (int i = 0; i < dimensionePool; i++) {
      this.compositori.add(new CompositoreModelli(this));
    }
  }

  /**
   * Ritorna il primo compositore disponibile se esiste.
   * @param isCompositoreJava true se si tratta del compositore Java, false se si tratta di quelli legacy
   * @param pathCompositore percorso del compositore eseguibile da porre in esecuzione
   *
   * @return compositore da utilizzare
   * @throws CompositoreException
   *         eccezione ritornata se non esistono compositori disponibili nel
   *         pool
   */
  public synchronized CompositoreModelli getCompositore(boolean isCompositoreJava)
      throws CompositoreException {
    CompositoreModelli compositoreModelli = null;
    if (this.compositori.size() == 0) {
      throw new CompositoreException(
          CompositoreException.CODICE_ERRORE_POOL_ESAURITO);
    }
    // se arrivo qui allora un compositore disponibile c'è
    compositoreModelli = this.compositori.remove(this.compositori.size() - 1);
    compositoreModelli.reset();
    compositoreModelli.setCompositoreJava(isCompositoreJava);
    compositoreModelli.setPathCompositore(ServizioCompositore.getPathCompositore(isCompositoreJava));

    return compositoreModelli;
  }

  /**
   * Permette di reinserire nuovamente il compositore nel pool dei compositori
   * disponibili.
   *
   * @param compositore
   *        compositore da reinserire nel pool
   */
  public synchronized void setCompositore(CompositoreModelli compositore) {
    this.compositori.add(compositore);
  }

}
