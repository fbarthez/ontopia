package ontopoly.components;

import java.util.Date;

import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.Topic;
import ontopoly.LockManager;
import ontopoly.OntopolySession;
import ontopoly.utils.OntopolyContext;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;

public abstract class LockPanel extends Panel {
  
  protected boolean lockedByOther;
  protected String lockedBy;
  protected String lockedAt;
  protected String lockKey;
  
  public LockPanel(String id, IModel topicModel, boolean shouldAcquireLock) {
    super(id);
    setModel(topicModel);
    
    // acquire lock unless read-only page
    if (!shouldAcquireLock)
      acquireLock();
    
    WebMarkupContainer container = new WebMarkupContainer("lockPanelContainer") {
      @Override 
      public boolean isVisible() {
        return lockedByOther;
      }      
    };
    container.setOutputMarkupId(true);
    
    container.add(new Label("lockMessage", lockedByOther ? new ResourceModel("lockPanel.message") : null));
    
    container.add(new Label("lockedByLabel", new ResourceModel("lockPanel.lockedByLabel")));
    container.add(new Label("lockedByValue", lockedBy));
    container.add(new Label("lockedAtLabel", new ResourceModel("lockPanel.lockedAtLabel")));
    container.add(new Label("lockedAtValue", lockedAt));
     
    final AbstractAjaxTimerBehavior timerBehavior = 
      new AbstractAjaxTimerBehavior(Duration.minutes(LockManager.DEFAULT_LOCK_REACQUIRE_TIMESPAN_MINUTES)) {
      @Override
      protected void onTimer(AjaxRequestTarget target) {
        boolean hadlock = !lockedByOther;
        //! System.out.println("Attempting to " + (hadlock ? "re" : "") + "acquire lock on " + (AbstractTopic)getModelObject());
        boolean gotlock = acquireLock();
        //! System.out.println("Got lock: " + hadlock + " " + gotlock);
        if ((hadlock && !gotlock)) {
          stop();
          onLockLost(target, (Topic)getModelObject());
        } else if (!hadlock && gotlock) {
          onLockWon(target, (Topic)getModelObject());
        }
      }
    };
    Button unlockButton = new Button("unlockButton");
    unlockButton.add(new AjaxFormComponentUpdatingBehavior("onclick") {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        LockManager lockManager = OntopolyContext.getLockManager();
        Topic topic = (Topic)getModelObject();
        lockManager.forcedUnlock(lockKey);
        timerBehavior.stop();
        onLockWon(target, topic);
      }
    });
    container.add(unlockButton);
    add(container);
   
    // add timer behaviour only if page was locked by us
    if (!lockedByOther) {
      // have page (re)acquire the lock just before it times out
      add(timerBehavior);
    }
  }
  
  @Override
  public boolean isVisible() {
    return lockedByOther;
  }
  
  protected boolean acquireLock() {
    // create lock id and lock key
    OntopolySession session = (OntopolySession)Session.get();
    String lockerId = session.getLockerId(getRequest());
    LockManager.Lock lock = session.lock((Topic)getModelObject(), lockerId);
    this.lockedBy = lock.getLockedBy();
    this.lockedAt = new Date(lock.getLockTime()).toString();
    this.lockKey = lock.getLockKey();
    if (!lock.ownedBy(lockerId)) {
      this.lockedByOther = true;
      //! System.out.println("Got lock: false: " + lock);
      return false;
    } else {
      //! System.out.println("Got lock: true" + lock);
      return true;
    }
  }
  
  public boolean isLockedByOther() {
    return lockedByOther;
  }
  
  /**
   * Called when the lock on the topic was lost.
   * @param target
   * @param topic
   */
  protected abstract void onLockLost(AjaxRequestTarget target, Topic topic);
  
  /**
   * Called when the lock on the topic was won.
   * @param target
   * @param topic
   */
  protected abstract void onLockWon(AjaxRequestTarget target, Topic topic);
  
}
