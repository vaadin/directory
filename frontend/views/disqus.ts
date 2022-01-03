
declare global { interface Window { DISQUS : any; } }

export function disqusReset(newIdentifier:string, newUrl:string, newTitle:string, visible: boolean = true) {
    console.log("RESET on "+(window.DISQUS)+": "+newIdentifier + ", "+newUrl+", "+newTitle);
    window.DISQUS.reset({
        reload: true,
        config: function () {
            this.page.identifier = newIdentifier;
            this.page.url = newUrl;
            this.page.title = newTitle;
        }
    });
    if (visible) {
        document.getElementsByClassName("discussions")[0].classList.add("active");
    } else {
        document.getElementsByClassName("discussions")[0].classList.remove("active");
    }
}