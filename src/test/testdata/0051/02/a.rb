# an example ruby file, with logs and exceptions

def log_examples
  system.print "This is not an error message."

  logger.log '[E-1] Example one, single quoted strings.'
  logger.warn "[E-2] Example two, double quoted strings."
  logger.error %q{[E-3] Example three, percent quoted strings, single quotes.}
  logger.error %Q{[E-4] Example four, percent quoted strings, double quotes, with parameters #{1 + 1}}
  logger.error %[\[E-5\] Example five, percent quoted strings, double quotes, with parameters #{1 + 1}]
  logger.error %Q<[E-6] Example six, percent quoted strings, double quotes, with parameters #{1 + 1}>
  logger.error %Q|[E-7] Example seven, percent quoted strings, double quotes, with parameters #{1 + 1}|

  system.print 'E-8'
  system.print "E-9"
  system.print "Not a __PLACEHOLDER__"
  # note placeholders do not work for % encoded strings or HEREDOC.

  a_method <<~DOC1, <<~DOC2
    first document content
  DOC1
    second document content
  DOC2

  logger.log <<~EG8
    [E-10] Example eight, heredoc log.
  EG8
end

def exception_examples

  raise ExceptionClass, '[E-11] Example one, single quoted strings.'
  raise ExceptionClass, "[E-12] Example two, double quoted strings."
  raise ExceptionClass,
    "[E-13] Example three, string on another line." \
    "with continuation on a line afterwards."
  raise ExceptionClass, <<HEREDOC
[E-14] Example four, heredoc type 1, double quote string.
HEREDOC
  raise ExceptionClass, <<-HEREDOC
[E-15] Example four, heredoc type 2, indented.
  HEREDOC
  raise ExceptionClass, <<~"HEREDOC"
    [E-16] Example five, heredoc type 3, squiggly.
  HEREDOC
  raise ExceptionClass, <<'HEREDOC'
[E-17] Example six, heredoc type 4, single quoted string.
HEREDOC
  raise ExceptionClass, <<-'HEREDOC'
[E-18] Example seven, heredoc type 5, single quoted string, indented.
  HEREDOC
  raise ExceptionClass, <<~'HEREDOC'
    [E-19] Example eight, heredoc type 6, single quoted string, squiggly.
  HEREDOC

end

def syntax_examples
  raise IOError, "[E-20] closed stream" if closed?
end