// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

class Type {

    foo() {
        return "";
    }
}

@Component({
  templateUrl: "./template.html"
})
export class MyComponent {
    bar: Type;
}
