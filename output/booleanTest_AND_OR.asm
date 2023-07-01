        Jump         $$main                    
        DLabel       $eat-location-zero        
        DataZ        8                         
        DLabel       $print-format-integer     
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-floating    
        DataC        37                        %% "%f"
        DataC        102                       
        DataC        0                         
        DLabel       $print-format-boolean     
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-newline     
        DataC        10                        %% "\n"
        DataC        0                         
        DLabel       $print-format-string      
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-character   
        DataC        37                        %% "%c"
        DataC        99                        
        DataC        0                         
        DLabel       $print-format-space       
        DataC        32                        %% " "
        DataC        0                         
        DLabel       $print-format-tabspace    
        DataC        9                         %% "\t"
        DataC        0                         
        DLabel       $boolean-true-string      
        DataC        116                       %% "true"
        DataC        114                       
        DataC        117                       
        DataC        101                       
        DataC        0                         
        DLabel       $boolean-false-string     
        DataC        102                       %% "false"
        DataC        97                        
        DataC        108                       
        DataC        115                       
        DataC        101                       
        DataC        0                         
        DLabel       $errors-general-message   
        DataC        82                        %% "Runtime error: %s\n"
        DataC        117                       
        DataC        110                       
        DataC        116                       
        DataC        105                       
        DataC        109                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        114                       
        DataC        114                       
        DataC        111                       
        DataC        114                       
        DataC        58                        
        DataC        32                        
        DataC        37                        
        DataC        115                       
        DataC        10                        
        DataC        0                         
        Label        $$general-runtime-error   
        PushD        $errors-general-message   
        Printf                                 
        Halt                                   
        DLabel       $errors-int-divide-by-zero 
        DataC        105                       %% "integer divide by zero"
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        103                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$i-divide-by-zero        
        PushD        $errors-int-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-float-divide-by-zero 
        DataC        102                       %% "floating divide by zero"
        DataC        108                       
        DataC        111                       
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        110                       
        DataC        103                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$f-divide-by-zero        
        PushD        $errors-float-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        8                         
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% and_true
        Label        -Operator-1-args          
        PushI        1                         
        PushI        1                         
        And                                    
        StoreC                                 
        PushD        $global-memory-block      
        PushI        1                         
        Add                                    %% and_false
        Label        -Operator-3-args          
        Label        -compare-2-arg1           
        PushI        5                         
        Label        -compare-2-arg2           
        PushI        5                         
        Label        -compare-2-sub            
        Subtract                               
        JumpFalse    -compare-2-true           
        Jump         -compare-2-false          
        Label        -compare-2-true           
        PushI        1                         
        Jump         -compare-2-join           
        Label        -compare-2-false          
        PushI        0                         
        Jump         -compare-2-join           
        Label        -compare-2-join           
        PushI        0                         
        And                                    
        StoreC                                 
        PushD        $global-memory-block      
        PushI        2                         
        Add                                    %% and_false2
        Label        -Operator-4-args          
        PushI        0                         
        PushI        0                         
        And                                    
        StoreC                                 
        PushD        $global-memory-block      
        PushI        3                         
        Add                                    %% and_false3
        Label        -Operator-6-args          
        Label        -compare-5-arg1           
        PushI        5                         
        Label        -compare-5-arg2           
        PushI        3                         
        Label        -compare-5-sub            
        Subtract                               
        JumpFalse    -compare-5-true           
        Jump         -compare-5-false          
        Label        -compare-5-true           
        PushI        1                         
        Jump         -compare-5-join           
        Label        -compare-5-false          
        PushI        0                         
        Jump         -compare-5-join           
        Label        -compare-5-join           
        PushI        0                         
        And                                    
        StoreC                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% and_true
        LoadC                                  
        JumpTrue     -print-boolean-7-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-7-join     
        Label        -print-boolean-7-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-7-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        1                         
        Add                                    %% and_false
        LoadC                                  
        JumpTrue     -print-boolean-8-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-8-join     
        Label        -print-boolean-8-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-8-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        2                         
        Add                                    %% and_false2
        LoadC                                  
        JumpTrue     -print-boolean-9-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-9-join     
        Label        -print-boolean-9-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-9-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        3                         
        Add                                    %% and_false3
        LoadC                                  
        JumpTrue     -print-boolean-10-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-10-join    
        Label        -print-boolean-10-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-10-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% or_true
        Label        -Operator-11-args         
        PushI        1                         
        PushI        1                         
        Or                                     
        StoreC                                 
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% or_true2
        Label        -Operator-12-args         
        PushI        1                         
        PushI        0                         
        Or                                     
        StoreC                                 
        PushD        $global-memory-block      
        PushI        6                         
        Add                                    %% or_true3
        Label        -Operator-13-args         
        PushI        0                         
        PushI        1                         
        Or                                     
        StoreC                                 
        PushD        $global-memory-block      
        PushI        7                         
        Add                                    %% or_false
        Label        -Operator-15-args         
        Label        -compare-14-arg1          
        PushI        5                         
        Label        -compare-14-arg2          
        PushI        3                         
        Label        -compare-14-sub           
        Subtract                               
        JumpFalse    -compare-14-true          
        Jump         -compare-14-false         
        Label        -compare-14-true          
        PushI        1                         
        Jump         -compare-14-join          
        Label        -compare-14-false         
        PushI        0                         
        Jump         -compare-14-join          
        Label        -compare-14-join          
        PushI        0                         
        And                                    
        StoreC                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% or_true
        LoadC                                  
        JumpTrue     -print-boolean-16-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-16-join    
        Label        -print-boolean-16-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-16-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% or_true2
        LoadC                                  
        JumpTrue     -print-boolean-17-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-17-join    
        Label        -print-boolean-17-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-17-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        6                         
        Add                                    %% or_true3
        LoadC                                  
        JumpTrue     -print-boolean-18-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-18-join    
        Label        -print-boolean-18-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-18-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        7                         
        Add                                    %% or_false
        LoadC                                  
        JumpTrue     -print-boolean-19-true    
        PushD        $boolean-false-string     
        Jump         -print-boolean-19-join    
        Label        -print-boolean-19-true    
        PushD        $boolean-true-string      
        Label        -print-boolean-19-join    
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   
