webpackJsonp([43],{GNsl:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var o=function(e,t){return"undefined"!=typeof getComputedStyle?getComputedStyle(e,null).getPropertyValue(t):e.style[t]},r=function(e){return o(e,"overflow")+o(e,"overflow-y")+o(e,"overflow-x")},i=function(e){if(!(e instanceof HTMLElement))return window;for(var t=e;t&&t!==document.body&&t!==document.documentElement&&t.parentNode;){if(/(scroll|auto)/.test(r(t)))return t;t=t.parentNode}return window};t.default=i},THSg:function(e,t,n){"use strict";function o(e,t,n){if(a(e))return!1;var o=void 0,r=void 0,f=void 0,u=void 0;if(void 0===t||t===window)o=window.pageYOffset,f=window.pageXOffset,r=o+window.innerHeight,u=f+window.innerWidth;else{var l=(0,i.default)(t);o=l.top,f=l.left,r=o+t.offsetHeight,u=f+t.offsetWidth}var c=(0,i.default)(e);return o<=c.top+e.offsetHeight+n.top&&r>=c.top-n.bottom&&f<=c.left+e.offsetWidth+n.left&&u>=c.left-n.right}Object.defineProperty(t,"__esModule",{value:!0}),t.default=o;var r=n("gNf8"),i=function(e){return e&&e.__esModule?e:{default:e}}(r),a=function(e){return null===e.offsetParent}},Tywg:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),n.d(t,"default",function(){return H});var o,r,i=n("YbOa"),a=n.n(i),f=n("U5hO"),u=n.n(f),l=n("EE81"),c=n.n(l),s=n("Jmyu"),d=n.n(s),p=n("/00i"),v=n.n(p),h=n("9wvh"),y=n.n(h),m=n("NmwX"),b=(n.n(m),n("0M1p")),g=n.n(b),w=n("f3tq"),O=n.n(w),j=n("cU1V"),L=n.n(j),_=[{label:"NSFC01-01B",page:20},{label:"NSFC01-02T",page:38},{label:"FAMILY_LAD",page:6}],T=function(e,t){return(Array(t).join("0")+e).slice(-t)},H=(o=Object(m.connect)(function(e){return{tech:e.tech}}))(r=function(e){function t(){var e,n,o;a()(this,t);for(var r=arguments.length,i=new Array(r),f=0;f<r;f++)i[f]=arguments[f];return d()(o,(n=o=d()(this,(e=v()(t)).call.apply(e,[this].concat(i))),o.timer=null,o.timerList=[],o.data=[],o.list=[],n))}return c()(t,[{key:"componentWillMount",value:function(){for(var e=this.props.location,t=g()("/tech/reader/:name").exec(e.pathname),n=t[1],o=_.filter(function(e){return e.label===n})[0].page,r=0;r<o;r++)this.data[r]={url:"/".concat(n,"/").concat(T(r+1,4),".jpg"),page:r},this.timerList[r]={img:r,time:0}}},{key:"componentDidMount",value:function(){}},{key:"render",value:function(){return y.a.createElement("div",{className:"content"},this.data.map(function(e,t){return y.a.createElement(O.a,{height:400,key:t,offsetBottom:100},y.a.createElement("img",{className:L.a.img,key:e.page,src:e.url,alt:"doc"}))}))}}]),u()(t,e),t}(h.Component))||r},bBgq:function(e,t,n){(function(t){function n(e,t,n){function r(t){var n=h,o=y;return h=y=void 0,_=t,b=e.apply(o,n)}function i(e){return _=e,g=setTimeout(c,t),T?r(e):b}function u(e){var n=e-L,o=e-_,r=t-n;return H?O(r,m-o):r}function l(e){var n=e-L,o=e-_;return void 0===L||n>=t||n<0||H&&o>=m}function c(){var e=j();if(l(e))return s(e);g=setTimeout(c,u(e))}function s(e){return g=void 0,z&&h?r(e):(h=y=void 0,b)}function d(){void 0!==g&&clearTimeout(g),_=0,h=L=y=g=void 0}function p(){return void 0===g?b:s(j())}function v(){var e=j(),n=l(e);if(h=arguments,y=this,L=e,n){if(void 0===g)return i(L);if(H)return g=setTimeout(c,t),r(L)}return void 0===g&&(g=setTimeout(c,t)),b}var h,y,m,b,g,L,_=0,T=!1,H=!1,z=!0;if("function"!=typeof e)throw new TypeError(f);return t=a(t)||0,o(n)&&(T=!!n.leading,H="maxWait"in n,m=H?w(a(n.maxWait)||0,t):m,z="trailing"in n?!!n.trailing:z),v.cancel=d,v.flush=p,v}function o(e){var t=typeof e;return!!e&&("object"==t||"function"==t)}function r(e){return!!e&&"object"==typeof e}function i(e){return"symbol"==typeof e||r(e)&&g.call(e)==l}function a(e){if("number"==typeof e)return e;if(i(e))return u;if(o(e)){var t="function"==typeof e.valueOf?e.valueOf():e;e=o(t)?t+"":t}if("string"!=typeof e)return 0===e?e:+e;e=e.replace(c,"");var n=d.test(e);return n||p.test(e)?v(e.slice(2),n?2:8):s.test(e)?u:+e}var f="Expected a function",u=NaN,l="[object Symbol]",c=/^\s+|\s+$/g,s=/^[-+]0x[0-9a-f]+$/i,d=/^0b[01]+$/i,p=/^0o[0-7]+$/i,v=parseInt,h="object"==typeof t&&t&&t.Object===Object&&t,y="object"==typeof self&&self&&self.Object===Object&&self,m=h||y||Function("return this")(),b=Object.prototype,g=b.toString,w=Math.max,O=Math.min,j=function(){return m.Date.now()};e.exports=n}).call(t,n("9AUj"))},cU1V:function(e,t){e.exports={view:"view___1bIZ1",img:"img___2gPrI"}},eB2W:function(e,t,n){var o,r;!function(i,a){o=a,void 0!==(r="function"==typeof o?o.call(t,n,t,e):o)&&(e.exports=r)}(0,function(){function e(e,t){return function(n,o,r,i){n[e]?n[e](o,r,i):n[t]&&n[t]("on"+o,r)}}return{add:e("addEventListener","attachEvent"),remove:e("removeEventListener","detachEvent")}})},f3tq:function(e,t,n){"use strict";function o(e){return e&&e.__esModule?e:{default:e}}function r(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}function i(e,t){if(!e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return!t||"object"!=typeof t&&"function"!=typeof t?e:t}function a(e,t){if("function"!=typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function, not "+typeof t);e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,enumerable:!1,writable:!0,configurable:!0}}),t&&(Object.setPrototypeOf?Object.setPrototypeOf(e,t):e.__proto__=t)}Object.defineProperty(t,"__esModule",{value:!0});var f=function(){function e(e,t){for(var n=0;n<t.length;n++){var o=t[n];o.enumerable=o.enumerable||!1,o.configurable=!0,"value"in o&&(o.writable=!0),Object.defineProperty(e,o.key,o)}}return function(t,n,o){return n&&e(t.prototype,n),o&&e(t,o),t}}(),u=n("9wvh"),l=o(u),c=n("/mFE"),s=o(c),d=n("3o31"),p=n("eB2W"),v=n("bBgq"),h=o(v),y=n("jcz/"),m=o(y),b=n("GNsl"),g=o(b),w=n("THSg"),O=o(w),j=function(e){function t(e){r(this,t);var n=i(this,(t.__proto__||Object.getPrototypeOf(t)).call(this,e));return n.lazyLoadHandler=n.lazyLoadHandler.bind(n),e.throttle>0&&(e.debounce?n.lazyLoadHandler=(0,h.default)(n.lazyLoadHandler,e.throttle):n.lazyLoadHandler=(0,m.default)(n.lazyLoadHandler,e.throttle)),n.state={visible:!1},n}return a(t,e),f(t,[{key:"componentDidMount",value:function(){this._mounted=!0;var e=this.getEventNode();this.lazyLoadHandler(),this.lazyLoadHandler.flush&&this.lazyLoadHandler.flush(),(0,p.add)(window,"resize",this.lazyLoadHandler),(0,p.add)(e,"scroll",this.lazyLoadHandler)}},{key:"componentWillReceiveProps",value:function(){this.state.visible||this.lazyLoadHandler()}},{key:"shouldComponentUpdate",value:function(e,t){return t.visible}},{key:"componentWillUnmount",value:function(){this._mounted=!1,this.lazyLoadHandler.cancel&&this.lazyLoadHandler.cancel(),this.detachListeners()}},{key:"getEventNode",value:function(){return(0,g.default)((0,d.findDOMNode)(this))}},{key:"getOffset",value:function(){var e=this.props,t=e.offset,n=e.offsetVertical,o=e.offsetHorizontal,r=e.offsetTop,i=e.offsetBottom,a=e.offsetLeft,f=e.offsetRight,u=e.threshold,l=u||t,c=n||l,s=o||l;return{top:r||c,bottom:i||c,left:a||s,right:f||s}}},{key:"lazyLoadHandler",value:function(){if(this._mounted){var e=this.getOffset(),t=(0,d.findDOMNode)(this),n=this.getEventNode();if((0,O.default)(t,n,e)){var o=this.props.onContentVisible;this.setState({visible:!0},function(){o&&o()}),this.detachListeners()}}}},{key:"detachListeners",value:function(){var e=this.getEventNode();(0,p.remove)(window,"resize",this.lazyLoadHandler),(0,p.remove)(e,"scroll",this.lazyLoadHandler)}},{key:"render",value:function(){var e=this.props,t=e.children,n=e.className,o=e.height,r=e.width,i=this.state.visible,a={height:o,width:r},f="LazyLoad"+(i?" is-visible":"")+(n?" "+n:"");return l.default.createElement(this.props.elementType,{className:f,style:a},i&&u.Children.only(t))}}]),t}(u.Component);t.default=j,j.propTypes={children:s.default.node.isRequired,className:s.default.string,debounce:s.default.bool,elementType:s.default.string,height:s.default.oneOfType([s.default.string,s.default.number]),offset:s.default.number,offsetBottom:s.default.number,offsetHorizontal:s.default.number,offsetLeft:s.default.number,offsetRight:s.default.number,offsetTop:s.default.number,offsetVertical:s.default.number,threshold:s.default.number,throttle:s.default.number,width:s.default.oneOfType([s.default.string,s.default.number]),onContentVisible:s.default.func},j.defaultProps={elementType:"div",debounce:!0,offset:0,offsetBottom:0,offsetHorizontal:0,offsetLeft:0,offsetRight:0,offsetTop:0,offsetVertical:0,throttle:250}},gNf8:function(e,t,n){"use strict";function o(e){var t=e.getBoundingClientRect();return{top:t.top+window.pageYOffset,left:t.left+window.pageXOffset}}Object.defineProperty(t,"__esModule",{value:!0}),t.default=o},"jcz/":function(e,t,n){(function(t){function n(e,t,n){function o(t){var n=h,o=y;return h=y=void 0,_=t,b=e.apply(o,n)}function i(e){return _=e,g=setTimeout(c,t),T?o(e):b}function a(e){var n=e-w,o=e-_,r=t-n;return H?j(r,m-o):r}function l(e){var n=e-w,o=e-_;return void 0===w||n>=t||n<0||H&&o>=m}function c(){var e=L();if(l(e))return s(e);g=setTimeout(c,a(e))}function s(e){return g=void 0,z&&h?o(e):(h=y=void 0,b)}function d(){void 0!==g&&clearTimeout(g),_=0,h=w=y=g=void 0}function p(){return void 0===g?b:s(L())}function v(){var e=L(),n=l(e);if(h=arguments,y=this,w=e,n){if(void 0===g)return i(w);if(H)return g=setTimeout(c,t),o(w)}return void 0===g&&(g=setTimeout(c,t)),b}var h,y,m,b,g,w,_=0,T=!1,H=!1,z=!0;if("function"!=typeof e)throw new TypeError(u);return t=f(t)||0,r(n)&&(T=!!n.leading,H="maxWait"in n,m=H?O(f(n.maxWait)||0,t):m,z="trailing"in n?!!n.trailing:z),v.cancel=d,v.flush=p,v}function o(e,t,o){var i=!0,a=!0;if("function"!=typeof e)throw new TypeError(u);return r(o)&&(i="leading"in o?!!o.leading:i,a="trailing"in o?!!o.trailing:a),n(e,t,{leading:i,maxWait:t,trailing:a})}function r(e){var t=typeof e;return!!e&&("object"==t||"function"==t)}function i(e){return!!e&&"object"==typeof e}function a(e){return"symbol"==typeof e||i(e)&&w.call(e)==c}function f(e){if("number"==typeof e)return e;if(a(e))return l;if(r(e)){var t="function"==typeof e.valueOf?e.valueOf():e;e=r(t)?t+"":t}if("string"!=typeof e)return 0===e?e:+e;e=e.replace(s,"");var n=p.test(e);return n||v.test(e)?h(e.slice(2),n?2:8):d.test(e)?l:+e}var u="Expected a function",l=NaN,c="[object Symbol]",s=/^\s+|\s+$/g,d=/^[-+]0x[0-9a-f]+$/i,p=/^0b[01]+$/i,v=/^0o[0-7]+$/i,h=parseInt,y="object"==typeof t&&t&&t.Object===Object&&t,m="object"==typeof self&&self&&self.Object===Object&&self,b=y||m||Function("return this")(),g=Object.prototype,w=g.toString,O=Math.max,j=Math.min,L=function(){return b.Date.now()};e.exports=o}).call(t,n("9AUj"))}});