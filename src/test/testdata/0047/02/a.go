// https://gobyexample.com/errors
// license: https://creativecommons.org/licenses/by/3.0/
// changes were made - added attribution and license, as per the license.

package main

import (
    "errors"
    "fmt"
)

func f1(arg int) (int, error) {
    if arg == 42 {

        return -1, errors.New("[E-1] can't work with 42")

    }

    return arg + 3, nil
}

type argError struct {
    arg  int
    prob string
}

func (e *argError) Error() string {
    return fmt.Sprintf("%d - %s", e.arg, e.prob)
}

func f2(arg int) (int, error) {
    if arg == 42 {

        return -1, &argError{arg, "can't work with it"}
    }
    if arg == 43 {
        return -1, &argError{arg, "E-2"}
    }
    if arg == 44 {
        return -1, &argError{arg, 'E-3'}
    }
    if arg == 45 {
        return -1, &argError{arg, `E-4`}
    }
    return arg + 3, nil
}

func main() {

    for _, i := range []int{7, 42} {
        if r, e := f1(i); e != nil {
            fmt.Println("f1 failed:", e)
        } else {
            fmt.Println("f1 worked:", r)
        }
    }
    for _, i := range []int{7, 42} {
        if r, e := f2(i); e != nil {
            fmt.Println("f2 failed:", e)
        } else {
            fmt.Println("f2 worked:", r)
        }
    }

    _, e := f2(42)
    if ae, ok := e.(*argError); ok {
        fmt.Println(ae.arg)
        fmt.Println(ae.prob)
    }
}

// from elsewhere
type ExampleType []string

func (p *ExampleType) exampleFunc(src interface{}) error {
    return errors.New("[E-5] an example error")
}