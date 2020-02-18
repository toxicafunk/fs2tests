package example

import fs2.Stream
import fs2.Chunk
import cats.effect.IO

object FS2Tests {
  val s0 = Stream.empty
  val s1 = Stream.emit(1)
  val s1a = Stream(1, 2, 3) // variadic
  val s1b = Stream.emits(List(1, 2, 3)) // accepts any Seq
  val eff = Stream.eval(IO { println("BEING RUN!!"); 1 + 1 })
  val ra = eff.compile.toVector // gather all output into a Vector
  val rb = eff.compile.drain // purely for effects
  val rc = eff.compile.fold(0)(_ + _) // run and accumulate some result
  val s1c = Stream.chunk(Chunk.doubles(Array(1.0, 2.0, 3.0)))
  val appendEx1 = Stream(1, 2, 3) ++ Stream.emit(42)
  val appendEx2 = Stream(1, 2, 3) ++ Stream.eval(IO.pure(4))
  val err = Stream.raiseError[IO](new Exception("oh noes!"))
  val err2 = Stream(1, 2, 3) ++ (throw new Exception("!@#$"))
  val err3 = Stream.eval(IO(throw new Exception("error in effect!!!")))
  val count = new java.util.concurrent.atomic.AtomicLong(0)
  val acquire = IO { println("incremented: " + count.incrementAndGet); () }
  val release = IO { println("decremented: " + count.decrementAndGet); () }

  def main(args: Array[String]): Unit = {
    s1.toList
    s1.toVector
    (s1a ++ Stream(4, 5)).toList
    s1a.map(_ + 1).toList
    s1a.filter(_ % 2 != 0).toList
    s1a.fold(0)(_ + _).toList
    Stream(None, Some(2), Some(3)).collect { case Some(i) => i }.toList
    Stream.range(0, 5).intersperse(42).toList
    s1a.flatMap(i => Stream(i, i)).toList
    s1a.repeat.take(9).toList
    s1a.repeatN(2).toList

    //eff.toList -- error
    eff.compile.toVector.unsafeRunSync()
    ra.unsafeRunSync() // etc
    s1c.mapChunks { ds =>
      val doubles = ds.toDoubles
      /* do things unboxed using doubles.{values,size} */
      doubles
    }
    appendEx1.toVector
    appendEx2.compile.toVector.unsafeRunSync()

    appendEx1.map(_ + 1).toList
    appendEx1.flatMap(i => Stream.emits(List(i, i))).toList

    try err.compile.toList.unsafeRunSync
    catch { case e: Exception => println(e) }

    try err2.toList
    catch { case e: Exception => println(e) }

    try err3.compile.drain.unsafeRunSync()
    catch { case e: Exception => println(e) }

    err
      .handleErrorWith { e =>
        Stream.emit(e.getMessage)
      }
      .compile
      .toList
      .unsafeRunSync()

    Stream
      .bracket(acquire)(_ => release)
      .flatMap(_ => Stream(1, 2, 3) ++ err)
      .compile
      .drain
      .unsafeRunSync()

    count.get
  }
}
