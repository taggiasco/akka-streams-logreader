package ch.taggiasco.streams


trait Particularity {
  def label: String
}

case class IdentityParticularity() extends Particularity {
  override def label: String = ""
}

case class SingleParticularity(element: String) extends Particularity {
  override def label: String = element
}

case class DualParticularity(first: String, second: String) extends Particularity {
  override def label: String = first + " - " + second
}
