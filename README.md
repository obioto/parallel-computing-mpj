# Pancake Sorter Algorithm

An iterative and a parallel approach to sort with prefix reversal. Also known as pancake sorting.

## Installation

* Iterative
    * You will find the `main(...)` method in `IterativeSolver`.
* Parallel
    * To use the parallel approach you first must install [MPJ Express](http://mpj-express.org).
    * Don't forget to set the environment variable `MPJ_HOME`.
    * To start via gradle
        * Execute the `run` command with gradle
    * To start via command line
        * `mpjrun.sh -np 5 ch.sebastianhaeni.pancake.ParallelSolver` (Linux / Mac)
        * `mpjrun.bat -np 5 ch.sebastianhaeni.pancake.ParallelSolver` (Windows)
