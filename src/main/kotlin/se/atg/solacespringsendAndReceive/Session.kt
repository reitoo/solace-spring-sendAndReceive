@file:Suppress("PackageDirectoryMismatch")
package brave.jms

import javax.jms.Session

val Session.delegate : Session get() = if (this is TracingSession) delegate else this;