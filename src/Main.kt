package phonebook

import java.io.File
import kotlin.math.floor
import kotlin.math.sqrt

class Person(val phone: String, val name: String)

object SearchingResult {
    var timeSearch = 0L
    var timeSort = 0L
    var timeLinearSearch = 0L
    var count = 0
}

object PhoneBook {
    private var directory = mutableListOf<Person>()
    private var findList = listOf<String>()
    private val hashTable = hashMapOf<String,String>()

    fun initDir(text: List<String>) {
        text.forEach { line -> line.split("\\s+".toRegex(), limit = 2).let { directory.add(Person(it[0], it[1])) } }
    }

    fun initFindList(text: List<String>) {
        findList = text
    }

    object Search {
        var stopped = false

        fun createHashTable() {
            val start = System.currentTimeMillis()
            for (item in directory) {
                hashTable[item.name] = item.phone
            }
            SearchingResult.timeSort = System.currentTimeMillis() - start
        }
        fun searchByHash() {
            val start = System.currentTimeMillis()
            var count = 0
            for (item in findList) {
                if (hashTable[item] != null) count++
            }
            SearchingResult.timeSearch = System.currentTimeMillis() - start
            SearchingResult.count = count
        }

        private fun jumpSearchValue(value: String): Int {
            var step = floor(sqrt(directory.size.toDouble())).toInt()
            var curr = 0
            var ind = 0
            while (curr <= directory.size - 1) {
                if (directory[curr].name == value) {
                    return curr
                } else if (directory[curr].name > value) {
                    ind = curr - 1
                    while (ind > curr - step && ind >= 0) {
                        if (directory[ind].name == value) return ind
                        ind = --ind
                    }
                    return -1
                }
                curr += step
                ind = directory.size - 1
            }

            while (ind > curr - step) {
                if (directory[ind].name == value) return ind
                ind = --ind
            }

            return -1
        }

        fun linearSearch() {
            var start = System.currentTimeMillis()
            var count = 0
            for (item in findList) {
                for (line in directory) {
                    if (line.name.contains(item)) {
                        count++
                        break
                    }
                }
            }
            SearchingResult.timeSearch = System.currentTimeMillis() - start
            SearchingResult.timeLinearSearch = SearchingResult.timeSearch
            SearchingResult.count = count
        }

        fun bubbleSort() {
            var start = System.currentTimeMillis()
            var flag: Boolean
            do {
                flag = false
                for (i in 1 until directory.size) {
                    if (directory[i].name < directory[i - 1].name) {
                        val swap = directory[i]
                        directory[i] = directory[i - 1]
                        directory[i - 1] = swap
                        flag = true
                    }
                    SearchingResult.timeSort = System.currentTimeMillis() - start
                    if (SearchingResult.timeSort > SearchingResult.timeLinearSearch * 10) {
                        stopped = true
                        return
                    }
                }
            } while (flag)
        }

        fun jumpSearch() {
            var count = 0
            if (!stopped) {
                var start = System.currentTimeMillis()
                for (item in findList) {
                    if (jumpSearchValue(item) > -1) count++
                }
                SearchingResult.timeSearch = System.currentTimeMillis() - start
                SearchingResult.count = count
            } else linearSearch()
        }

        private fun partition(start: Int, end: Int): Int {
            var start = start
            var end = end

            while (start < end) {
                while (start < end) {
                    if (directory[start].name > directory[end].name) {
                        val swap = directory[start]
                        directory[start] = directory[end]
                        directory[end] = swap
                        break
                    }
                    end -= 1
                }
                while (start < end) {
                    if (directory[start].name > directory[end].name) {
                        val swap = directory[start]
                        directory[start] = directory[end]
                        directory[end] = swap
                        break
                    }
                    start += 1
                }
            }
            return start
        }

        fun quicksort(start: Int = -1, end: Int = -1) {
            var timestart = System.currentTimeMillis()
            var start = start
            var end = end

            if (start == -1) start = 0
            if (end == -1) end = directory.size

            if (start < end) {
                val i = partition(start, end - 1)
                quicksort(start, i)
                quicksort(i + 1, end)
            }
            SearchingResult.timeSort = System.currentTimeMillis() - timestart
        }

        private fun binarySearchValue(value: String): Int {
            var index = 0
            var end = directory.size - 1

            while (index <= end) {
                val center: Int = (index + end) / 2

                if (value == directory[center].name) {
                    return center
                } else if (value < directory[center].name) {
                    end = center - 1
                } else if (value > directory[center].name) {
                    index = center + 1
                }
            }
            return -1
        }

        fun binarySearch() {
            var count = 0
            var start = System.currentTimeMillis()
            for (item in findList) {
                if (binarySearchValue(item) > -1) count++
            }
            SearchingResult.timeSearch = System.currentTimeMillis() - start
            SearchingResult.count = count
        }

        private fun printResult(preProcessing: String) {
            var timeString = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", SearchingResult.timeSort + SearchingResult.timeSearch)
            println("Found ${SearchingResult.count} / ${findList.size} entries. Time taken: $timeString")
            if (SearchingResult.timeSort != 0L) {
                timeString = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", SearchingResult.timeSort)
                print("$preProcessing time: $timeString")
                if (stopped) { print(" - STOPPED, moved to linear search"); stopped = false}
                timeString = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", SearchingResult.timeSearch)
                println("\nSearching time: $timeString")
            }
        }
        fun findAll ( searchFunction: () -> Unit, sortFunction: (() -> Unit)? = null, preProcessing: String = "Sorting") {
            if (sortFunction != null) sortFunction() else SearchingResult.timeSort = 0
            searchFunction()
            printResult(preProcessing)
        }
    }
}

fun main() {
    PhoneBook.initDir(File("F:\\2022 Все проекты\\kotlin\\phonebook\\directory.txt").readLines())
    PhoneBook.initFindList(File("F:\\2022 Все проекты\\kotlin\\phonebook\\find.txt").readLines())

    println("Start searching (linear search)...")
    PhoneBook.Search.findAll(PhoneBook.Search::linearSearch)

    println("\nStart searching (bubble sort + jump search)...")
    PhoneBook.Search.findAll(PhoneBook.Search::jumpSearch,PhoneBook.Search::bubbleSort)

    println("\nStart searching (quick sort + binary search)...")
    PhoneBook.Search.findAll(PhoneBook.Search::binarySearch,PhoneBook.Search::quicksort)

    println("\nStart searching (hash table)...")
    PhoneBook.Search.findAll(PhoneBook.Search::searchByHash,PhoneBook.Search::createHashTable,"Creating")
}