theory Example
imports Protocol_Main
begin

ML\<open>
  val global_ctxt = @{context}
    |> Config.put show_markup false
\<close>

operation_setup (auto) pretty = \<open>
  (* string -> string *)
  YXML.content_of o Syntax.string_of_term global_ctxt o Syntax.read_term global_ctxt
\<close>

end
