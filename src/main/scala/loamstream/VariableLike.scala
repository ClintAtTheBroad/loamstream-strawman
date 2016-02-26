package loamstream

trait VariableLike[A] extends Variable[A] {
  private[this] val lock = new AnyRef
  
  private[this] var _value: Option[A] = None
  
  override def foreach(f: A => Any)(implicit scheduler: Scheduler): Subscription = {
    val subscription = super.foreach(f)
    
    valueOption match {
      case Some(v) => publish(v)
      case None => ()
    }
    
    subscription
  }
  
  override def toPipeline(implicit scheduler: Scheduler): Pipeline[A] = {
    val result = Pipeline.empty[A]
    
    foreach(result.accept(_)) 
    
    result
  }
  
  override def valueOption: Option[A] = lock.synchronized(_value)
  
  override def value: A = valueOption.get
  
  override def set(a: A)(implicit scheduler: Scheduler): Unit = {
    updateSilently(a)
    
    publish(a)
  }
  
  override def modifyOrElse(default: => A)(f: A => A)(implicit emitter: Scheduler): Unit = lock.synchronized {
    val newVal = valueOption.map(f).getOrElse(default)
    
    set(newVal)
  }
  
  override def accept(a: A)(implicit scheduler: Scheduler): Unit = {
    scheduler.run { () => 
      set(a)
    }
  }
  
  private[loamstream] def updateSilently(a: A): Unit = updateValue(Some(a))
  
  private def updateValue(newValue: Option[A]): Unit = {
    lock.synchronized {
      _value = newValue
    }
  }
}