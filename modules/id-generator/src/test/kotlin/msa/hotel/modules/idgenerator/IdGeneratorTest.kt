package msa.hotel.modules.idgenerator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class IdGeneratorTest :
    BehaviorSpec({
        val idGenerator = IdGenerator()

        fun generateIdList(
            idGenerator: IdGenerator,
            count: Int,
        ): MutableList<ULong?> {
            var c = count
            val idList = mutableListOf<ULong?>()

            while (c-- > 0) {
                idList.add(idGenerator.generate())
            }

            return idList
        }

        Given("동시에 다량의 아이디를 생성할 때") {
            When("여러 스레드에서 generate()를 호출하면") {
                val executorService = Executors.newFixedThreadPool(10)
                val futures = mutableListOf<Future<MutableList<ULong?>>>()
                val repeatCount = 1000
                val idCount = 1000

                for (i in 0 until repeatCount) {
                    futures.add(executorService.submit(Callable { generateIdList(idGenerator, idCount) }))
                }

                Then("각 스레드 내에서 증가하고 전체도 중복 없이 생성된다") {
                    val result = mutableListOf<ULong?>()
                    for (future in futures) {
                        val idList = future.get()
                        for (i in 1 until idList.size) {
                            idList[i]!!.shouldBeGreaterThan(idList[i - 1]!!)
                        }
                        result.addAll(idList)
                    }

                    result.distinct().count().toLong() shouldBe (repeatCount * idCount).toLong()
                    executorService.shutdown()
                }
            }
        }
    })
