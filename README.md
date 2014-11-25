# clojure-dojo

This is project is based on a chestnut template. The only think I added is the nrepl middleware and a branch containing a first draft for a possible approach to the dojo.

So if you prefer you can easily recreate the poject using chestnut directly:

```sh
lein new chestnut clojure-dojo
```

## Development

Start a REPL (in a terminal: `lein repl`, or from Emacs: open a
clj/cljs file in the project, then do `M-x cider-jack-in`. Make sure
CIDER is up to date).

In the REPL do

```clojure
(run)
(browser-repl)
```

The call to `(run)` does two things, it starts the webserver at port
10555, and also the Figwheel server which takes care of live reloading
ClojureScript code and CSS. Give them some time to start.

Running `(browser-repl)` starts the Weasel REPL server, and drops you
into a ClojureScript REPL. Evaluating expressions here will only work
once you've loaded the page, so the browser can connect to Weasel.

When you see the line `Successfully compiled "resources/public/app.js"
in 21.36 seconds.`, you're ready to go. Browse to
`http://localhost:10555` and enjoy.

**Attention: It is not longer needed to run `lein figwheel`
  separately. This is now taken care of behind the scenes**

## Trying it out

If all is well you now have a browser window saying 'Hello Chestnut',
and a REPL prompt that looks like `cljs.user=>`.

Open `resources/public/css/style.css` and change some styling of the
H1 element. Notice how it's updated instantly in the browser.

Open `src/cljs/clojure-dojo/core.cljs`, and change `dom/h1` to
`dom/h2`. As soon as you save the file, your browser is updated.

In the REPL, type

```
(ns clojure-dojo.core)
(swap! app-state assoc :text "Interactivity FTW")
```

Notice again how the browser updates.

##Om Basics

If you can find the time check out the excellent Om tutorial:

https://github.com/swannodette/om/wiki/Basic-Tutorial

We will use excerpts from that tutorial below to try to
illustrate some of the basic ideas.

The `om.core/root` is already provided by the chestnut template. It
binds the Om render loop to a specific element in the  DOM.

You will also have noticed that there typically is an atom to store the
applications state. This is also already in the template.

Let's assume for now your app state looks like this:

```clojure
(def app-state
  (atom
    {:contacts
     [{:first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
      {:first "Alyssa" :middle-initial "P" :last "Hacker" :email "aphacker@mit.edu"}
      {:first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
      {:first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
      {:first "Cy" :middle-initial "D" :last "Effect" :email "bugs@mit.edu"}
      {:first "Lem" :middle-initial "E" :last "Tweakit" :email
      "morebugs@mit.edu"}]}))
```      
We can now build an Om component to render this list of contacts.

```clojure
(defn contacts-view [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (dom/h2 nil "Contact list")
        (apply dom/ul nil
          (om/build-all contact-view (:contacts app)))))))
```
In order to build an Om component we implement one of the Om
protocols. In this case the
[IRender](https://github.com/swannodette/om/wiki/Documentation#irender)
protocol.

You will also notice that we use a couple of DOM helper methods in the
`om.dom` namespace. Finally we use `om\build-all` to create a OM
component for every contact stored in the application state atom.

The `contact-view` component could look like this:

```clojure
(defn contact-view [contact owner]
  (reify
    om/IRender
    (render [this]
      (dom/li #js{:className "contact"} (str (:first contact) " "
      (:last contact))))))
```
### What's the nil/#js business?

The `om.dom` API allows you to pass in properties into the DOM
elements to be created. They have to be a JavaScript array/object. `#js{...}`
is a so called reader literal to create a native JavaScript object.
You can use this to set CSS classes and declare event handlers
(React.js does event delegation ...)

### How do I change my application state

Have a look a [`om/transact!`](https://github.com/swannodette/om/wiki/Documentation#transact)

Here an example of how to add a contact to our application state:
```clojure
(om/transact! app :contacts #(conj % new-contact)))
```

### What's are Cursors

In Om you don't interact with the application state directly but with
a cursor into the application state.

The `contact` in the `contact-view` example above is one of these cursors.

Think of it as a description of how to get to a specific element in a map--like a path description.
You ask why are we doing this? It frees us from having to know how
your application state is structured ("a map nested tree levels deep, all
contacts are stored at ...") but instead you can just pass in a cursor
to the contacts into your Om component and work with that without
having to know where exactly the contacts are stored. You can even
update/modify the contacts just by using the cursor and one of
the `om/transact!` or `om/update!` functions.

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
